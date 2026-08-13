import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_svg/flutter_svg.dart';
import 'package:http/http.dart' as http;
import 'package:image_picker/image_picker.dart';
import 'package:liquid_glass_widgets/liquid_glass_widgets.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'archive_icons.dart';
import 'models.dart';

const _bg = Color(0xFFFFFFFF);
const _ink = Color(0xFF111111);
const _muted = Color(0xFF888888);
const _faint = Color(0xFFBBBBBB);
const _surface = Color(0xFFF8F8F8);
const _button = Color(0xFF222222);
const _danger = Color(0xFFE5484D);
const _radiusLg = 28.0;
const _radiusMd = 20.0;
const _radiusSm = 12.0;

enum AppPage { home, compose, detail, achi, album, stats, settings, apiEdit }

class _PageTransitionToken {
  const _PageTransitionToken({
    required this.page,
    required this.reverse,
    required this.sequence,
  });

  final AppPage page;
  final bool reverse;
  final int sequence;

  @override
  bool operator ==(Object other) =>
      other is _PageTransitionToken &&
      other.page == page &&
      other.reverse == reverse &&
      other.sequence == sequence;

  @override
  int get hashCode => Object.hash(page, reverse, sequence);
}

final _rootMessengerKey = GlobalKey<ScaffoldMessengerState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  PaintingBinding.instance.imageCache.maximumSize = 120;
  PaintingBinding.instance.imageCache.maximumSizeBytes = 64 << 20;
  SystemChrome.setSystemUIOverlayStyle(
    const SystemUiOverlayStyle(
      statusBarColor: _bg,
      statusBarIconBrightness: Brightness.dark,
      systemNavigationBarColor: _bg,
      systemNavigationBarIconBrightness: Brightness.dark,
    ),
  );
  await LiquidGlassWidgets.initialize();
  runApp(
    LiquidGlassWidgets.wrap(
      brightnessResolver: Theme.maybeBrightnessOf,
      child: const ArchiveApp(),
    ),
  );
}

class ArchiveApp extends StatelessWidget {
  const ArchiveApp({super.key});

  @override
  Widget build(BuildContext context) => MaterialApp(
    title: 'Archive',
    debugShowCheckedModeBanner: false,
    scaffoldMessengerKey: _rootMessengerKey,
    theme: ThemeData(
      useMaterial3: true,
      fontFamily: 'sans-serif',
      scaffoldBackgroundColor: _bg,
      splashFactory: NoSplash.splashFactory,
      splashColor: Colors.transparent,
      highlightColor: Colors.transparent,
      colorScheme: ColorScheme.fromSeed(
        seedColor: _button,
        surface: _bg,
        brightness: Brightness.light,
      ),
    ),
    home: const ArchiveShell(),
  );
}

class ArchiveShell extends StatefulWidget {
  const ArchiveShell({super.key});

  @override
  State<ArchiveShell> createState() => _ArchiveShellState();
}

class _ArchiveShellState extends State<ArchiveShell> {
  final _scaffoldKey = GlobalKey<ScaffoldState>();
  final List<ArchiveEntry> _entries = initialEntries();
  final Map<String, Uint8List> _localImages = {};
  final List<_ChatMessage> _achiMessages = [
    const _ChatMessage(role: 'ai', text: '嗨，我是 Achi。今天过得怎么样？随时和我聊聊。'),
  ];
  final _achiReplies = const [
    '听起来不错，然后呢？',
    '把这一刻记下来，之后回看会很有意思。',
    '我懂这种感觉。',
    '要不要拍张照片，把现在的心情存进 Archive？',
    '慢慢说，我在听。',
    '平凡的日子也值得被记录。',
  ];
  AppPage _page = AppPage.home;
  bool _drawerOpen = false;
  bool _reverseTransition = false;
  int _transitionSequence = 0;
  bool _edgeGestureActive = false;
  double _edgeDragDistance = 0;
  String? _detailId;
  List<ApiProvider> _providers = defaultProviders();
  ApiProvider? _editingProvider;
  Timer? _achiTimer;

  @override
  void initState() {
    super.initState();
    _restoreProviders();
    WidgetsBinding.instance.addPostFrameCallback((_) => _warmInitialAssets());
  }

  Future<void> _warmInitialAssets() async {
    await precacheImage(const AssetImage('assets/images/cafe.webp'), context);
  }

  @override
  void dispose() {
    _achiTimer?.cancel();
    super.dispose();
  }

  Future<void> _restoreProviders() async {
    final preferences = await SharedPreferences.getInstance();
    final saved = preferences.getString('archive.providers');
    if (saved == null || saved.isEmpty) {
      return;
    }
    try {
      final parsed = ApiProvider.decodeList(saved);
      if (mounted && parsed.isNotEmpty) setState(() => _providers = parsed);
    } catch (_) {}
  }

  Future<void> _persistProviders() async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString(
      'archive.providers',
      ApiProvider.encodeList(_providers),
    );
  }

  void _showToast(String message) {
    _rootMessengerKey.currentState?.hideCurrentSnackBar();
    _rootMessengerKey.currentState?.showSnackBar(
      SnackBar(
        content: Text(message, style: const TextStyle(fontSize: 12)),
        behavior: SnackBarBehavior.floating,
        duration: const Duration(milliseconds: 1800),
        backgroundColor: const Color(0xDD111111),
        elevation: 0,
        shape: const StadiumBorder(),
        margin: const EdgeInsets.fromLTRB(48, 0, 48, 62),
      ),
    );
  }

  ArchiveEntry? get _detailEntry {
    for (final entry in _entries) {
      if (entry.id == _detailId) return entry;
    }
    return null;
  }

  void _navigate(AppPage target, {bool reverse = false}) => setState(() {
    _drawerOpen = false;
    _reverseTransition = reverse;
    _transitionSequence++;
    _page = target;
    if (target == AppPage.home) _detailId = null;
  });

  void _goBack() {
    if (_drawerOpen) {
      setState(() => _drawerOpen = false);
      return;
    }
    if (_page == AppPage.home) return;
    _navigate(
      _page == AppPage.apiEdit ? AppPage.settings : AppPage.home,
      reverse: true,
    );
  }

  void _handleSystemBack(bool didPop, Object? result) {
    if (!didPop) _goBack();
  }

  void _onEdgeDragStart(DragStartDetails details) {
    _edgeGestureActive = details.localPosition.dx <= 28 && !_drawerOpen;
    _edgeDragDistance = 0;
  }

  void _onEdgeDragUpdate(DragUpdateDetails details) {
    if (!_edgeGestureActive) return;
    _edgeDragDistance += details.primaryDelta ?? 0;
  }

  void _onEdgeDragEnd(DragEndDetails details) {
    if (!_edgeGestureActive) return;
    final shouldCommit =
        _edgeDragDistance > 54 ||
        details.primaryVelocity != null && details.primaryVelocity! > 760;
    if (shouldCommit) {
      if (_page == AppPage.home) {
        setState(() => _drawerOpen = true);
      } else {
        _goBack();
      }
    }
    _edgeGestureActive = false;
    _edgeDragDistance = 0;
  }

  void _openDetail(String id) => setState(() {
    _detailId = id;
    _drawerOpen = false;
    _reverseTransition = false;
    _transitionSequence++;
    _page = AppPage.detail;
  });

  void _saveEntry({
    required String text,
    required List<Uint8List> imageBytes,
    required String location,
    required String weather,
    required String mood,
  }) {
    final references = <String>[];
    for (final bytes in imageBytes) {
      final key =
          'local-${DateTime.now().microsecondsSinceEpoch}-${references.length}';
      _localImages[key] = bytes;
      references.add('local:$key');
    }
    setState(() {
      _entries.insert(
        0,
        ArchiveEntry(
          id: DateTime.now().microsecondsSinceEpoch.toString(),
          timestamp: DateTime.now(),
          text: text,
          images: references,
          location: location,
          weather: weather,
          mood: mood,
        ),
      );
      _reverseTransition = true;
      _transitionSequence++;
      _page = AppPage.home;
    });
  }

  void _addComment(String entryId, String text, ArchiveComment? replyTo) {
    final entry = _entries.firstWhere((item) => item.id == entryId);
    if (replyTo == null) {
      entry.comments.add(
        ArchiveComment(
          id: 'c${DateTime.now().microsecondsSinceEpoch}',
          name: 'Serein',
          avatar: avatarMe,
          text: text,
          time: '刚刚',
        ),
      );
    } else {
      replyTo.replies.add(
        ArchiveReply(name: 'Serein', avatar: avatarMe, text: text, time: '刚刚'),
      );
    }
    setState(() {});
  }

  void _sendAchi(String text) {
    setState(() => _achiMessages.add(_ChatMessage(role: 'user', text: text)));
    _achiTimer?.cancel();
    _achiTimer = Timer(const Duration(milliseconds: 700), () {
      if (!mounted) {
        return;
      }
      setState(
        () => _achiMessages.add(
          _ChatMessage(
            role: 'ai',
            text: _achiReplies[Random().nextInt(_achiReplies.length)],
          ),
        ),
      );
    });
  }

  void _openProvider(ApiProvider? provider) => setState(() {
    _editingProvider =
        provider?.copy() ??
        ApiProvider(
          id: 'p${DateTime.now().microsecondsSinceEpoch}',
          name: '',
          format: 'openai',
          url: '',
          key: '',
        );
    _reverseTransition = false;
    _transitionSequence++;
    _page = AppPage.apiEdit;
  });

  Future<void> _saveProvider(ApiProvider provider) async {
    final index = _providers.indexWhere((item) => item.id == provider.id);
    setState(() {
      if (index < 0) {
        _providers.add(provider);
      } else {
        _providers[index] = provider;
      }
      _reverseTransition = true;
      _transitionSequence++;
      _page = AppPage.settings;
      _editingProvider = null;
    });
    await _persistProviders();
    if (mounted) _showToast('已保存供应商');
  }

  Future<void> _deleteProvider(ApiProvider provider) async {
    setState(() {
      _providers.removeWhere((item) => item.id == provider.id);
      _reverseTransition = true;
      _transitionSequence++;
      _page = AppPage.settings;
      _editingProvider = null;
    });
    await _persistProviders();
    if (mounted) _showToast('已删除供应商');
  }

  Widget _pageBody() {
    switch (_page) {
      case AppPage.home:
        return _HomePage(
          entries: _entries,
          images: _localImages,
          onMenu: () => setState(() => _drawerOpen = true),
          onCreate: () => _navigate(AppPage.compose),
          onOpenEntry: _openDetail,
        );
      case AppPage.compose:
        return _ComposePage(onCancel: _goBack, onSave: _saveEntry);
      case AppPage.detail:
        final entry = _detailEntry;
        return entry == null
            ? _HomePage(
                entries: _entries,
                images: _localImages,
                onMenu: () => setState(() => _drawerOpen = true),
                onCreate: () => _navigate(AppPage.compose),
                onOpenEntry: _openDetail,
              )
            : _DetailPage(
                key: ValueKey(entry.id),
                entry: entry,
                localImages: _localImages,
                onBack: _goBack,
                onAddComment: (text, reply) =>
                    _addComment(entry.id, text, reply),
              );
      case AppPage.achi:
        return _AchiPage(
          messages: _achiMessages,
          onBack: _goBack,
          onSend: _sendAchi,
        );
      case AppPage.album:
        return _AlbumPage(
          entries: _entries,
          localImages: _localImages,
          onBack: _goBack,
          onOpenEntry: _openDetail,
        );
      case AppPage.stats:
        return _StatsPage(entries: _entries, onBack: _goBack);
      case AppPage.settings:
        return _SettingsPage(
          providers: _providers,
          onBack: _goBack,
          onEditProvider: _openProvider,
          onToast: _showToast,
        );
      case AppPage.apiEdit:
        return _ApiEditorPage(
          key: ValueKey(_editingProvider?.id),
          provider: _editingProvider!,
          isNew: !_providers.any((item) => item.id == _editingProvider!.id),
          onBack: _goBack,
          onSave: _saveProvider,
          onDelete: _deleteProvider,
          onToast: _showToast,
        );
    }
  }

  @override
  Widget build(BuildContext context) => PopScope(
    canPop: _page == AppPage.home && !_drawerOpen,
    onPopInvokedWithResult: _handleSystemBack,
    child: Scaffold(
      key: _scaffoldKey,
      body: ColoredBox(
        color: const Color(0xFFF0F0F0),
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 480),
            child: ColoredBox(
              color: _bg,
              child: SafeArea(
                child: GestureDetector(
                  behavior: HitTestBehavior.translucent,
                  onHorizontalDragStart: _onEdgeDragStart,
                  onHorizontalDragUpdate: _onEdgeDragUpdate,
                  onHorizontalDragEnd: _onEdgeDragEnd,
                  child: Stack(
                    children: [
                      AnimatedSwitcher(
                        duration: const Duration(milliseconds: 260),
                        reverseDuration: const Duration(milliseconds: 220),
                        switchInCurve: Curves.easeOutCubic,
                        switchOutCurve: Curves.easeInCubic,
                        // Keep only the incoming page in the paint stack. This
                        // prevents a delayed outgoing page from leaving a ghost.
                        layoutBuilder: (currentChild, _) => ClipRect(
                          child: currentChild ?? const SizedBox.shrink(),
                        ),
                        transitionBuilder: (child, animation) {
                          final key = child.key;
                          final reverse = key is ValueKey<_PageTransitionToken>
                              ? key.value.reverse
                              : false;
                          final offset = reverse
                              ? const Offset(-.045, 0)
                              : const Offset(.045, 0);
                          return SlideTransition(
                            position:
                                Tween<Offset>(begin: offset, end: Offset.zero)
                                    .chain(
                                      CurveTween(curve: Curves.easeOutCubic),
                                    )
                                    .animate(animation),
                            child: child,
                          );
                        },
                        child: KeyedSubtree(
                          key: ValueKey(
                            _PageTransitionToken(
                              page: _page,
                              reverse: _reverseTransition,
                              sequence: _transitionSequence,
                            ),
                          ),
                          child: _pageBody(),
                        ),
                      ),
                      _SideDrawer(
                        visible: _drawerOpen,
                        onClose: () => setState(() => _drawerOpen = false),
                        onSelect: _navigate,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    ),
  );
}

class _TopHeader extends StatelessWidget {
  const _TopHeader({required this.left, required this.center, this.right});
  final Widget left;
  final Widget center;
  final Widget? right;
  @override
  Widget build(BuildContext context) => SizedBox(
    height: 64,
    child: Padding(
      padding: const EdgeInsets.fromLTRB(14, 12, 14, 4),
      child: Stack(
        alignment: Alignment.center,
        children: [
          Align(alignment: Alignment.centerLeft, child: left),
          Center(child: center),
          Align(
            alignment: Alignment.centerRight,
            child: right ?? const SizedBox(width: 52),
          ),
        ],
      ),
    ),
  );
}

class _Avatar extends StatelessWidget {
  const _Avatar({this.size = 32, this.url = avatarMe});
  final double size;
  final String url;

  @override
  Widget build(BuildContext context) {
    final fallback = ColoredBox(
      color: _surface,
      child: Center(
        child: ArchiveIcon('achi', size: size * .54, color: _muted),
      ),
    );
    final child = url.startsWith('asset:')
        ? SvgPicture.asset(
            url.substring(6),
            width: size,
            height: size,
            fit: BoxFit.cover,
            placeholderBuilder: (_) => fallback,
          )
        : CachedNetworkImage(
            imageUrl: url,
            width: size,
            height: size,
            fit: BoxFit.cover,
            memCacheWidth: (size * MediaQuery.devicePixelRatioOf(context))
                .round(),
            fadeInDuration: const Duration(milliseconds: 180),
            placeholder: (_, _) => fallback,
            errorWidget: (_, _, _) => fallback,
          );
    return RepaintBoundary(
      child: ClipOval(
        child: SizedBox(width: size, height: size, child: child),
      ),
    );
  }
}

class _CircularIconButton extends StatelessWidget {
  const _CircularIconButton({
    required this.iconName,
    required this.onTap,
    this.dark = false,
    this.enabled = true,
    this.glass = true,
    this.size = 38,
  });
  final String iconName;
  final VoidCallback? onTap;
  final bool dark;
  final bool enabled;
  final bool glass;
  final double size;

  VoidCallback? _callback() => enabled && onTap != null
      ? () {
          HapticFeedback.lightImpact();
          onTap?.call();
        }
      : null;

  @override
  Widget build(BuildContext context) {
    final callback = _callback();
    final icon = ArchiveIcon(
      iconName,
      size: size * .47,
      color: enabled ? (dark ? _bg : _ink) : _faint,
    );
    final background = dark ? _button : _surface;
    if (!glass) {
      return Material(
        color: background,
        shape: const CircleBorder(),
        child: InkWell(
          onTap: callback,
          customBorder: const CircleBorder(),
          child: SizedBox(
            width: size,
            height: size,
            child: Center(child: icon),
          ),
        ),
      );
    }
    // The package's transparent mode is the documented pattern for controls
    // over an existing surface: it retains liquid stretch/glow on press without
    // adding an opaque white glass plate on Archive's white canvas.
    return DecoratedBox(
      decoration: BoxDecoration(color: background, shape: BoxShape.circle),
      child: ClipOval(
        child: GlassButton.custom(
          onTap: callback ?? () {},
          enabled: callback != null,
          width: size,
          height: size,
          style: GlassButtonStyle.transparent,
          stretch: .15,
          child: icon,
        ),
      ),
    );
  }
}

class _HomePage extends StatelessWidget {
  const _HomePage({
    required this.entries,
    required this.images,
    required this.onMenu,
    required this.onCreate,
    required this.onOpenEntry,
  });
  final List<ArchiveEntry> entries;
  final Map<String, Uint8List> images;
  final VoidCallback onMenu;
  final VoidCallback onCreate;
  final ValueChanged<String> onOpenEntry;

  @override
  Widget build(BuildContext context) => Column(
    children: [
      Padding(
        padding: const EdgeInsets.fromLTRB(14, 16, 14, 8),
        child: Row(
          children: [
            InkWell(
              onTap: () {
                HapticFeedback.selectionClick();
                onMenu();
              },
              borderRadius: BorderRadius.circular(18),
              child: const Row(
                children: [
                  _Avatar(),
                  SizedBox(width: 9),
                  Text(
                    'Serein',
                    style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
            ),
            const Spacer(),
            _CircularIconButton(
              iconName: 'plus',
              onTap: onCreate,
              dark: true,
              size: 34,
            ),
          ],
        ),
      ),
      Expanded(
        child: ListView.builder(
          padding: const EdgeInsets.fromLTRB(10, 4, 20, 80),
          addAutomaticKeepAlives: false,
          itemCount: entries.length,
          itemBuilder: (context, index) => _FeedItem(
            key: ValueKey(entries[index].id),
            entry: entries[index],
            localImages: images,
            delay: index * 60,
            onTap: () => onOpenEntry(entries[index].id),
          ),
        ),
      ),
    ],
  );
}

class _FeedItem extends StatelessWidget {
  const _FeedItem({
    super.key,
    required this.entry,
    required this.localImages,
    required this.delay,
    required this.onTap,
  });
  final ArchiveEntry entry;
  final Map<String, Uint8List> localImages;
  final int delay;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final shortText = entry.text.length > 100
        ? '${entry.text.substring(0, 100)}...'
        : entry.text;
    return TweenAnimationBuilder<double>(
      duration: Duration(milliseconds: 420 + min(delay, 300)),
      tween: Tween(begin: 0, end: 1),
      curve: Curves.easeOutCubic,
      builder: (context, opacity, child) => Opacity(
        opacity: opacity,
        child: Transform.translate(
          offset: Offset(0, (1 - opacity) * 12),
          child: child,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.only(top: 36),
        child: InkWell(
          onTap: () {
            HapticFeedback.selectionClick();
            onTap();
          },
          borderRadius: BorderRadius.circular(_radiusMd),
          child: Padding(
            padding: const EdgeInsets.symmetric(vertical: 1),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                SizedBox(
                  width: 46,
                  child: Padding(
                    padding: const EdgeInsets.only(top: 2),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.baseline,
                      textBaseline: TextBaseline.alphabetic,
                      children: [
                        Text(
                          entry.timestamp.day.toString().padLeft(2, '0'),
                          style: const TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(width: 4),
                        Text(
                          monthShort(entry.timestamp.month),
                          style: const TextStyle(
                            fontSize: 10,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(width: 20),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (shortText.isNotEmpty)
                        Padding(
                          padding: const EdgeInsets.only(bottom: 10),
                          child: Text(
                            shortText,
                            style: const TextStyle(fontSize: 14, height: 1.55),
                          ),
                        ),
                      if (entry.images.isNotEmpty)
                        _ImageGrid(
                          references: entry.images,
                          localImages: localImages,
                          heightLimit: 300,
                        ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _ImageGrid extends StatelessWidget {
  const _ImageGrid({
    required this.references,
    required this.localImages,
    this.heightLimit,
    this.largeRadius = false,
  });
  final List<String> references;
  final Map<String, Uint8List> localImages;
  final double? heightLimit;
  final bool largeRadius;

  @override
  Widget build(BuildContext context) {
    final shown = references.take(9).toList();
    if (shown.isEmpty) {
      return const SizedBox.shrink();
    }
    final count = shown.length;
    final crossAxisCount = count == 1 ? 1 : (count == 2 || count == 4 ? 2 : 3);
    final double ratio = count == 1
        ? 4 / 3
        : (count == 2 ? 2.0 : (count == 3 ? 3.0 : 1.0));
    final radius = largeRadius ? 22.0 : _radiusMd;
    final grid = AspectRatio(
      aspectRatio: ratio,
      child: ClipRRect(
        borderRadius: BorderRadius.circular(radius),
        child: GridView.builder(
          physics: const NeverScrollableScrollPhysics(),
          padding: EdgeInsets.zero,
          gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: crossAxisCount,
            crossAxisSpacing: 3,
            mainAxisSpacing: 3,
          ),
          addAutomaticKeepAlives: false,
          addRepaintBoundaries: false,
          itemCount: shown.length,
          itemBuilder: (context, index) => _ArchiveImage(
            reference: shown[index],
            localImages: localImages,
            radius: count == 1 ? radius : 0,
          ),
        ),
      ),
    );
    return heightLimit == null || count != 1
        ? grid
        : ConstrainedBox(
            constraints: BoxConstraints(maxHeight: heightLimit!),
            child: grid,
          );
  }
}

class _ArchiveImage extends StatelessWidget {
  const _ArchiveImage({
    required this.reference,
    required this.localImages,
    this.radius = 0,
  });
  final String reference;
  final Map<String, Uint8List> localImages;
  final double radius;

  @override
  Widget build(BuildContext context) => LayoutBuilder(
    builder: (context, constraints) {
      final width = constraints.maxWidth.isFinite
          ? (constraints.maxWidth * MediaQuery.devicePixelRatioOf(context))
                .round()
          : null;
      final memory = reference.startsWith('local:')
          ? localImages[reference.substring(6)]
          : null;
      final placeholder = const ColoredBox(
        color: _surface,
        child: Center(child: ArchiveIcon('image', size: 22, color: _faint)),
      );
      final Widget image;
      if (memory != null) {
        image = Image.memory(
          memory,
          fit: BoxFit.cover,
          cacheWidth: width,
          filterQuality: FilterQuality.medium,
          gaplessPlayback: true,
        );
      } else if (reference.startsWith('asset:')) {
        image = Image.asset(
          reference.substring(6),
          fit: BoxFit.cover,
          cacheWidth: width,
          filterQuality: FilterQuality.medium,
          gaplessPlayback: true,
          errorBuilder: (_, _, _) => placeholder,
        );
      } else {
        image = CachedNetworkImage(
          imageUrl: reference,
          fit: BoxFit.cover,
          memCacheWidth: width,
          fadeInDuration: const Duration(milliseconds: 180),
          placeholder: (_, _) => placeholder,
          errorWidget: (_, _, _) => placeholder,
        );
      }
      final clipped = radius > 0
          ? ClipRRect(borderRadius: BorderRadius.circular(radius), child: image)
          : image;
      return RepaintBoundary(child: clipped);
    },
  );
}

class _ComposePage extends StatefulWidget {
  const _ComposePage({required this.onCancel, required this.onSave});
  final VoidCallback onCancel;
  final void Function({
    required String text,
    required List<Uint8List> imageBytes,
    required String location,
    required String weather,
    required String mood,
  })
  onSave;
  @override
  State<_ComposePage> createState() => _ComposePageState();
}

class _ComposePageState extends State<_ComposePage> {
  final _text = TextEditingController();
  final _picker = ImagePicker();
  final List<Uint8List> _images = [];
  String _location = '';
  String _weather = '';
  String _mood = '';
  bool get _canSubmit => _text.text.trim().isNotEmpty || _images.isNotEmpty;

  @override
  void initState() {
    super.initState();
    _text.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _text.dispose();
    super.dispose();
  }

  Future<void> _pickImages() async {
    final picked = await _picker.pickMultiImage(imageQuality: 88);
    if (picked.isEmpty) return;
    final bytes = await Future.wait(picked.map((file) => file.readAsBytes()));
    if (mounted) setState(() => _images.addAll(bytes));
  }

  Future<void> _choose(String type) async {
    final String title;
    final List<_SheetOption> options;
    String current;
    if (type == 'location') {
      title = '选择地点';
      current = _location;
      options = [
        const _SheetOption('', '不显示位置', ''),
        ...locations.map((value) => _SheetOption(value, value, 'map-pin')),
      ];
    } else if (type == 'weather') {
      title = '选择天气';
      current = _weather;
      options = [
        const _SheetOption('', '不显示', ''),
        ...weatherLabels.entries.map(
          (item) =>
              _SheetOption(item.key, item.value, weatherIconName(item.key)),
        ),
      ];
    } else {
      title = '选择心情';
      current = _mood;
      options = [
        const _SheetOption('', '不显示', ''),
        ...moodLabels.entries.map(
          (item) => _SheetOption(item.key, item.value, moodIconName(item.key)),
        ),
      ];
    }
    final chosen = await showModalBottomSheet<String>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) =>
          _OptionSheet(title: title, current: current, options: options),
    );
    if (chosen == null || !mounted) return;
    setState(() {
      if (type == 'location') _location = chosen;
      if (type == 'weather') _weather = chosen;
      if (type == 'mood') _mood = chosen;
    });
  }

  void _submit() {
    if (!_canSubmit) return;
    widget.onSave(
      text: _text.text.trim(),
      imageBytes: List.of(_images),
      location: _location,
      weather: _weather,
      mood: _mood,
    );
  }

  @override
  Widget build(BuildContext context) => Stack(
    children: [
      Column(
        children: [
          const SizedBox(height: 4),
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(24, 60, 24, 24),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextField(
                    controller: _text,
                    autofocus: true,
                    minLines: 5,
                    maxLines: 14,
                    cursorColor: _ink,
                    style: const TextStyle(fontSize: 15, height: 1.55),
                    decoration: const InputDecoration(
                      hintText: "What's on your mind?",
                      hintStyle: TextStyle(color: _faint),
                      border: InputBorder.none,
                      contentPadding: EdgeInsets.zero,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Wrap(
                    spacing: 10,
                    runSpacing: 10,
                    children: [
                      ...List.generate(
                        _images.length,
                        (index) => _PreviewImage(
                          bytes: _images[index],
                          onDelete: () =>
                              setState(() => _images.removeAt(index)),
                        ),
                      ),
                      InkWell(
                        onTap: _pickImages,
                        borderRadius: BorderRadius.circular(_radiusSm),
                        child: Ink(
                          width: 76,
                          height: 76,
                          decoration: BoxDecoration(
                            color: _surface,
                            borderRadius: BorderRadius.circular(_radiusSm),
                          ),
                          child: const ArchiveIcon(
                            'image',
                            color: _muted,
                            size: 23,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      _ToolPill(
                        iconName: 'map-pin',
                        label: _location.isEmpty ? '添加地点' : _location,
                        hasValue: _location.isNotEmpty,
                        onTap: () => _choose('location'),
                        onClear: () => setState(() => _location = ''),
                      ),
                      _ToolPill(
                        iconName: 'cloud-sun',
                        label: _weather.isEmpty
                            ? '天气'
                            : weatherLabels[_weather]!,
                        hasValue: _weather.isNotEmpty,
                        onTap: () => _choose('weather'),
                        onClear: () => setState(() => _weather = ''),
                      ),
                      _ToolPill(
                        iconName: 'smile',
                        label: _mood.isEmpty ? '心情' : moodLabels[_mood]!,
                        hasValue: _mood.isNotEmpty,
                        onTap: () => _choose('mood'),
                        onClear: () => setState(() => _mood = ''),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
      Positioned(
        top: 16,
        left: 16,
        child: _CircularIconButton(iconName: 'close', onTap: widget.onCancel),
      ),
      Positioned(
        top: 16,
        right: 16,
        child: _CircularIconButton(
          iconName: 'check',
          onTap: _submit,
          dark: true,
          enabled: _canSubmit,
        ),
      ),
    ],
  );
}

class _PreviewImage extends StatelessWidget {
  const _PreviewImage({required this.bytes, required this.onDelete});
  final Uint8List bytes;
  final VoidCallback onDelete;
  @override
  Widget build(BuildContext context) => SizedBox(
    width: 76,
    height: 76,
    child: Stack(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(_radiusSm),
          child: Image.memory(bytes, width: 76, height: 76, fit: BoxFit.cover),
        ),
        Positioned(
          top: 4,
          right: 4,
          child: Material(
            color: const Color(0x66000000),
            shape: const CircleBorder(),
            child: InkWell(
              onTap: onDelete,
              customBorder: const CircleBorder(),
              child: const SizedBox(
                width: 18,
                height: 18,
                child: ArchiveIcon('close', size: 12, color: Colors.white),
              ),
            ),
          ),
        ),
      ],
    ),
  );
}

class _ToolPill extends StatelessWidget {
  const _ToolPill({
    required this.iconName,
    required this.label,
    required this.hasValue,
    required this.onTap,
    required this.onClear,
  });
  final String iconName;
  final String label;
  final bool hasValue;
  final VoidCallback onTap;
  final VoidCallback onClear;
  @override
  Widget build(BuildContext context) => Material(
    color: _surface,
    borderRadius: BorderRadius.circular(999),
    child: InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(999),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            ArchiveIcon(iconName, size: 14, color: hasValue ? _ink : _muted),
            const SizedBox(width: 6),
            ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 130),
              child: Text(
                label,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  fontSize: 12,
                  color: hasValue ? _ink : _muted,
                  fontWeight: hasValue ? FontWeight.w500 : null,
                ),
              ),
            ),
            if (hasValue) ...[
              const SizedBox(width: 5),
              GestureDetector(
                onTap: onClear,
                child: const Text(
                  '×',
                  style: TextStyle(fontSize: 14, color: _faint, height: 1),
                ),
              ),
            ],
          ],
        ),
      ),
    ),
  );
}

class _SheetOption {
  const _SheetOption(this.value, this.label, this.iconName);
  final String value;
  final String label;
  final String iconName;
}

class _OptionSheet extends StatelessWidget {
  const _OptionSheet({
    required this.title,
    required this.current,
    required this.options,
  });
  final String title;
  final String current;
  final List<_SheetOption> options;
  @override
  Widget build(BuildContext context) => Container(
    constraints: BoxConstraints(
      maxHeight: MediaQuery.sizeOf(context).height * .7,
    ),
    decoration: const BoxDecoration(
      color: _bg,
      borderRadius: BorderRadius.vertical(top: Radius.circular(_radiusLg)),
    ),
    child: Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 36,
          height: 4,
          margin: const EdgeInsets.only(top: 10, bottom: 12),
          decoration: BoxDecoration(
            color: const Color(0xFFE5E5E5),
            borderRadius: BorderRadius.circular(4),
          ),
        ),
        Text(title, style: const TextStyle(fontSize: 12, color: _muted)),
        const SizedBox(height: 6),
        Flexible(
          child: ListView.builder(
            shrinkWrap: true,
            padding: const EdgeInsets.only(bottom: 16),
            itemCount: options.length,
            itemBuilder: (context, index) {
              final option = options[index];
              final selected = option.value == current;
              return ListTile(
                onTap: () {
                  HapticFeedback.selectionClick();
                  Navigator.pop(context, option.value);
                },
                contentPadding: const EdgeInsets.symmetric(horizontal: 24),
                dense: true,
                minVerticalPadding: 7,
                leading: option.iconName.isEmpty
                    ? const SizedBox(width: 17)
                    : ArchiveIcon(option.iconName, size: 17, color: _muted),
                title: Text(option.label, style: const TextStyle(fontSize: 14)),
                trailing: selected
                    ? const ArchiveIcon('check', size: 18, color: _ink)
                    : const SizedBox(width: 18),
              );
            },
          ),
        ),
      ],
    ),
  );
}

class _DetailPage extends StatefulWidget {
  const _DetailPage({
    super.key,
    required this.entry,
    required this.localImages,
    required this.onBack,
    required this.onAddComment,
  });
  final ArchiveEntry entry;
  final Map<String, Uint8List> localImages;
  final VoidCallback onBack;
  final void Function(String text, ArchiveComment? replyTo) onAddComment;
  @override
  State<_DetailPage> createState() => _DetailPageState();
}

class _DetailPageState extends State<_DetailPage> {
  final _comment = TextEditingController();
  final _scroll = ScrollController();
  ArchiveComment? _replyTo;
  @override
  void initState() {
    super.initState();
    _comment.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _comment.dispose();
    _scroll.dispose();
    super.dispose();
  }

  void _send() {
    final text = _comment.text.trim();
    if (text.isEmpty) {
      return;
    }
    widget.onAddComment(text, _replyTo);
    _comment.clear();
    setState(() => _replyTo = null);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scroll.hasClients) {
        _scroll.animateTo(
          _scroll.position.maxScrollExtent,
          duration: const Duration(milliseconds: 280),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) => Column(
    children: [
      _TopHeader(
        left: _BackButton(onTap: widget.onBack),
        center: const SizedBox.shrink(),
      ),
      Expanded(
        child: SingleChildScrollView(
          controller: _scroll,
          padding: const EdgeInsets.fromLTRB(22, 4, 22, 28),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Row(
                children: [
                  _Avatar(size: 38),
                  SizedBox(width: 10),
                  Text(
                    'Serein',
                    style: TextStyle(fontSize: 14, fontWeight: FontWeight.w600),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              if (widget.entry.text.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(bottom: 14),
                  child: Text(
                    widget.entry.text,
                    style: const TextStyle(fontSize: 14, height: 1.7),
                  ),
                ),
              if (widget.entry.images.isNotEmpty)
                _ImageGrid(
                  references: widget.entry.images,
                  localImages: widget.localImages,
                  heightLimit: 400,
                  largeRadius: true,
                ),
              Padding(
                padding: const EdgeInsets.only(top: 18),
                child: Text(
                  fullDate(widget.entry.timestamp),
                  style: const TextStyle(
                    fontSize: 11,
                    color: _muted,
                    letterSpacing: .2,
                  ),
                ),
              ),
              if (widget.entry.location.isNotEmpty ||
                  widget.entry.weather.isNotEmpty ||
                  widget.entry.mood.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 8),
                  child: Wrap(
                    spacing: 12,
                    runSpacing: 8,
                    children: [
                      if (widget.entry.location.isNotEmpty)
                        _MetaChip(
                          iconName: 'map-pin',
                          label: widget.entry.location,
                        ),
                      if (widget.entry.weather.isNotEmpty)
                        _MetaChip(
                          iconName: weatherIconName(widget.entry.weather),
                          label: weatherLabels[widget.entry.weather]!,
                        ),
                      if (widget.entry.mood.isNotEmpty)
                        _MetaChip(
                          iconName: moodIconName(widget.entry.mood),
                          label: moodLabels[widget.entry.mood]!,
                        ),
                    ],
                  ),
                ),
              const SizedBox(height: 26),
              Row(
                crossAxisAlignment: CrossAxisAlignment.baseline,
                textBaseline: TextBaseline.alphabetic,
                children: [
                  const Text(
                    '评论',
                    style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                  ),
                  const SizedBox(width: 5),
                  Text(
                    '${entryCommentCount(widget.entry)}',
                    style: const TextStyle(
                      fontSize: 11,
                      color: _muted,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              if (widget.entry.comments.isEmpty)
                const Padding(
                  padding: EdgeInsets.only(bottom: 8),
                  child: Text(
                    '还没有评论，来说点什么吧',
                    style: TextStyle(fontSize: 12, color: _faint),
                  ),
                )
              else
                ...widget.entry.comments.map(
                  (item) => _CommentItem(
                    comment: item,
                    onReply: () => setState(() => _replyTo = item),
                  ),
                ),
            ],
          ),
        ),
      ),
      _CommentBar(
        controller: _comment,
        replyTo: _replyTo,
        onCancelReply: () => setState(() => _replyTo = null),
        onSend: _send,
      ),
    ],
  );
}

class _MetaChip extends StatelessWidget {
  const _MetaChip({required this.iconName, required this.label});
  final String iconName;
  final String label;
  @override
  Widget build(BuildContext context) => Row(
    mainAxisSize: MainAxisSize.min,
    children: [
      ArchiveIcon(iconName, size: 13, color: _muted),
      const SizedBox(width: 4),
      Text(label, style: const TextStyle(fontSize: 11, color: _muted)),
    ],
  );
}

class _CommentItem extends StatelessWidget {
  const _CommentItem({required this.comment, required this.onReply});
  final ArchiveComment comment;
  final VoidCallback onReply;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 16),
    child: Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _Avatar(size: 28, url: comment.avatar),
        const SizedBox(width: 9),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Text(
                    comment.name,
                    style: const TextStyle(
                      fontSize: 12,
                      color: _muted,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(width: 7),
                  Text(
                    comment.time,
                    style: const TextStyle(fontSize: 10, color: _faint),
                  ),
                ],
              ),
              const SizedBox(height: 1),
              Text(
                comment.text,
                style: const TextStyle(fontSize: 13, height: 1.55),
              ),
              if (comment.replies.isNotEmpty)
                Padding(
                  padding: const EdgeInsets.only(top: 10),
                  child: Column(
                    children: comment.replies
                        .map(
                          (reply) => Padding(
                            padding: const EdgeInsets.only(bottom: 9),
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                _Avatar(size: 20, url: reply.avatar),
                                const SizedBox(width: 7),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      RichText(
                                        text: TextSpan(
                                          style: const TextStyle(
                                            fontSize: 12.5,
                                            color: _ink,
                                            height: 1.5,
                                            fontFamily: 'Arial',
                                          ),
                                          children: [
                                            TextSpan(
                                              text: '${reply.name}：',
                                              style: const TextStyle(
                                                color: _muted,
                                                fontWeight: FontWeight.w600,
                                              ),
                                            ),
                                            TextSpan(text: reply.text),
                                          ],
                                        ),
                                      ),
                                      Text(
                                        reply.time,
                                        style: const TextStyle(
                                          fontSize: 10,
                                          color: _faint,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        )
                        .toList(),
                  ),
                ),
            ],
          ),
        ),
        Material(
          color: Colors.transparent,
          shape: const CircleBorder(),
          child: InkWell(
            onTap: onReply,
            customBorder: const CircleBorder(),
            child: const SizedBox(
              width: 28,
              height: 28,
              child: Center(
                child: ArchiveIcon('reply', size: 16, color: _faint),
              ),
            ),
          ),
        ),
      ],
    ),
  );
}

class _CommentBar extends StatelessWidget {
  const _CommentBar({
    required this.controller,
    required this.replyTo,
    required this.onCancelReply,
    required this.onSend,
  });
  final TextEditingController controller;
  final ArchiveComment? replyTo;
  final VoidCallback onCancelReply;
  final VoidCallback onSend;
  @override
  Widget build(BuildContext context) {
    final active = controller.text.trim().isNotEmpty;
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 10, 16, 10),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (replyTo != null)
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
              margin: const EdgeInsets.only(bottom: 8),
              decoration: BoxDecoration(
                color: _surface,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: Text(
                      '回复 @${replyTo!.name}',
                      style: const TextStyle(fontSize: 11, color: _muted),
                    ),
                  ),
                  InkWell(
                    onTap: onCancelReply,
                    child: const Text(
                      '×',
                      style: TextStyle(fontSize: 14, color: _faint),
                    ),
                  ),
                ],
              ),
            ),
          Row(
            children: [
              const _Avatar(size: 28),
              const SizedBox(width: 9),
              Expanded(
                child: TextField(
                  controller: controller,
                  onSubmitted: (_) => onSend(),
                  style: const TextStyle(fontSize: 13),
                  decoration: InputDecoration(
                    hintText: replyTo == null
                        ? '写下评论…'
                        : '回复 @${replyTo!.name}',
                    hintStyle: const TextStyle(color: _faint),
                    isDense: true,
                    contentPadding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 9,
                    ),
                    filled: true,
                    fillColor: _surface,
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(18),
                      borderSide: BorderSide.none,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 9),
              _CircularIconButton(
                iconName: 'send',
                onTap: onSend,
                dark: true,
                enabled: active,
                size: 34,
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _BackButton extends StatelessWidget {
  const _BackButton({required this.onTap});
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => TextButton.icon(
    onPressed: onTap,
    icon: const ArchiveIcon('back', size: 22),
    label: const Text('Back'),
    style: TextButton.styleFrom(
      foregroundColor: _ink,
      minimumSize: Size.zero,
      tapTargetSize: MaterialTapTargetSize.shrinkWrap,
      padding: const EdgeInsets.symmetric(horizontal: 0, vertical: 7),
      textStyle: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
    ),
  );
}

class _ChatMessage {
  const _ChatMessage({required this.role, required this.text});
  final String role;
  final String text;
}

class _AchiPage extends StatefulWidget {
  const _AchiPage({
    required this.messages,
    required this.onBack,
    required this.onSend,
  });
  final List<_ChatMessage> messages;
  final VoidCallback onBack;
  final ValueChanged<String> onSend;
  @override
  State<_AchiPage> createState() => _AchiPageState();
}

class _AchiPageState extends State<_AchiPage> {
  final _input = TextEditingController();
  final _scroll = ScrollController();
  @override
  void initState() {
    super.initState();
    _input.addListener(() => setState(() {}));
    WidgetsBinding.instance.addPostFrameCallback((_) => _scrollToEnd());
  }

  @override
  void didUpdateWidget(covariant _AchiPage oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.messages.length != widget.messages.length) {
      _scrollToEnd();
    }
  }

  void _scrollToEnd() => WidgetsBinding.instance.addPostFrameCallback((_) {
    if (_scroll.hasClients) {
      _scroll.animateTo(
        _scroll.position.maxScrollExtent,
        duration: const Duration(milliseconds: 220),
        curve: Curves.easeOut,
      );
    }
  });
  void _send() {
    final text = _input.text.trim();
    if (text.isEmpty) {
      return;
    }
    widget.onSend(text);
    _input.clear();
  }

  @override
  void dispose() {
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Column(
    children: [
      _TopHeader(
        left: _BackButton(onTap: widget.onBack),
        center: const Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            ArchiveIcon('achi', size: 17),
            SizedBox(width: 6),
            Text(
              'Achi',
              style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
            ),
            SizedBox(width: 6),
            _AiTag(),
          ],
        ),
      ),
      Expanded(
        child: ListView.builder(
          controller: _scroll,
          padding: const EdgeInsets.fromLTRB(22, 12, 22, 18),
          itemCount: widget.messages.length,
          itemBuilder: (context, index) =>
              _ChatBubble(message: widget.messages[index]),
        ),
      ),
      Padding(
        padding: const EdgeInsets.fromLTRB(16, 10, 16, 10),
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: _input,
                onSubmitted: (_) => _send(),
                style: const TextStyle(fontSize: 13),
                decoration: InputDecoration(
                  hintText: '和 Achi 聊聊…',
                  hintStyle: const TextStyle(color: _faint),
                  isDense: true,
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 14,
                    vertical: 9,
                  ),
                  filled: true,
                  fillColor: _surface,
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(18),
                    borderSide: BorderSide.none,
                  ),
                ),
              ),
            ),
            const SizedBox(width: 9),
            _CircularIconButton(
              iconName: 'send',
              onTap: _send,
              dark: true,
              enabled: _input.text.trim().isNotEmpty,
              size: 34,
            ),
          ],
        ),
      ),
    ],
  );
}

class _AiTag extends StatelessWidget {
  const _AiTag();
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
    decoration: BoxDecoration(
      color: _button,
      borderRadius: BorderRadius.circular(999),
    ),
    child: const Text(
      'AI',
      style: TextStyle(
        color: _bg,
        fontSize: 9,
        fontWeight: FontWeight.w600,
        letterSpacing: .5,
      ),
    ),
  );
}

class _ChatBubble extends StatelessWidget {
  const _ChatBubble({required this.message});
  final _ChatMessage message;
  @override
  Widget build(BuildContext context) {
    final ai = message.role == 'ai';
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Row(
        mainAxisAlignment: ai ? MainAxisAlignment.start : MainAxisAlignment.end,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (ai) ...[
            Container(
              width: 28,
              height: 28,
              alignment: Alignment.center,
              decoration: const BoxDecoration(
                color: _surface,
                shape: BoxShape.circle,
              ),
              child: const ArchiveIcon('achi', size: 15),
            ),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Container(
              constraints: const BoxConstraints(maxWidth: 290),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: ai ? _surface : _button,
                borderRadius: BorderRadius.only(
                  topLeft: Radius.circular(ai ? 6 : 18),
                  topRight: Radius.circular(ai ? 18 : 6),
                  bottomLeft: const Radius.circular(18),
                  bottomRight: const Radius.circular(18),
                ),
              ),
              child: Text(
                message.text,
                style: TextStyle(
                  fontSize: 13,
                  color: ai ? _ink : _bg,
                  height: 1.55,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _AlbumPage extends StatelessWidget {
  const _AlbumPage({
    required this.entries,
    required this.localImages,
    required this.onBack,
    required this.onOpenEntry,
  });
  final List<ArchiveEntry> entries;
  final Map<String, Uint8List> localImages;
  final VoidCallback onBack;
  final ValueChanged<String> onOpenEntry;
  @override
  Widget build(BuildContext context) {
    final photos = <_AlbumPhoto>[];
    for (final entry in entries) {
      for (final reference in entry.images) {
        photos.add(_AlbumPhoto(reference, entry.id));
      }
    }
    return Column(
      children: [
        _TopHeader(
          left: _BackButton(onTap: onBack),
          center: const Text(
            '相册',
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
          ),
        ),
        Expanded(
          child: photos.isEmpty
              ? const Center(
                  child: Text(
                    '还没有照片',
                    style: TextStyle(fontSize: 12, color: _faint),
                  ),
                )
              : Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.fromLTRB(22, 8, 22, 12),
                      child: Align(
                        alignment: Alignment.centerLeft,
                        child: Text(
                          '共 ${photos.length} 张照片',
                          style: const TextStyle(fontSize: 11, color: _faint),
                        ),
                      ),
                    ),
                    Expanded(
                      child: GridView.builder(
                        padding: const EdgeInsets.fromLTRB(22, 0, 22, 60),
                        gridDelegate:
                            const SliverGridDelegateWithFixedCrossAxisCount(
                              crossAxisCount: 3,
                              crossAxisSpacing: 3,
                              mainAxisSpacing: 3,
                            ),
                        itemCount: photos.length,
                        itemBuilder: (context, index) {
                          final photo = photos[index];
                          return InkWell(
                            onTap: () => onOpenEntry(photo.entryId),
                            child: ClipRRect(
                              borderRadius: BorderRadius.circular(10),
                              child: _ArchiveImage(
                                reference: photo.reference,
                                localImages: localImages,
                              ),
                            ),
                          );
                        },
                      ),
                    ),
                  ],
                ),
        ),
      ],
    );
  }
}

class _AlbumPhoto {
  const _AlbumPhoto(this.reference, this.entryId);
  final String reference;
  final String entryId;
}

class _StatsPage extends StatelessWidget {
  const _StatsPage({required this.entries, required this.onBack});
  final List<ArchiveEntry> entries;
  final VoidCallback onBack;
  @override
  Widget build(BuildContext context) {
    final imageCount = entries.fold<int>(
      0,
      (total, entry) => total + entry.images.length,
    );
    final commentCount = entries.fold<int>(
      0,
      (total, entry) => total + entryCommentCount(entry),
    );
    final today = DateTime.now();
    final sevenDays = List.generate(
      7,
      (index) => DateTime(
        today.year,
        today.month,
        today.day,
      ).subtract(Duration(days: 6 - index)),
    );
    final dailyCount = sevenDays
        .map(
          (day) => entries
              .where(
                (entry) =>
                    entry.timestamp.year == day.year &&
                    entry.timestamp.month == day.month &&
                    entry.timestamp.day == day.day,
              )
              .length,
        )
        .toList();
    final maxCount = max(1, dailyCount.fold<int>(0, max));
    const weekdays = ['一', '二', '三', '四', '五', '六', '日'];
    return Column(
      children: [
        _TopHeader(
          left: _BackButton(onTap: onBack),
          center: const Text(
            '统计',
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
          ),
        ),
        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(22, 8, 22, 60),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    _StatCard(number: entries.length, label: '记录'),
                    const SizedBox(width: 10),
                    _StatCard(number: imageCount, label: '照片'),
                    const SizedBox(width: 10),
                    _StatCard(number: commentCount, label: '评论'),
                  ],
                ),
                const Padding(
                  padding: EdgeInsets.only(top: 22, bottom: 12),
                  child: Text(
                    '最近 7 天记录',
                    style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
                  ),
                ),
                Container(
                  height: 142,
                  padding: const EdgeInsets.fromLTRB(16, 18, 16, 14),
                  decoration: BoxDecoration(
                    color: _surface,
                    borderRadius: BorderRadius.circular(_radiusMd),
                  ),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: List.generate(7, (index) {
                      final count = dailyCount[index];
                      return Expanded(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.end,
                          children: [
                            Container(
                              width: 20,
                              height: count == 0
                                  ? 4
                                  : max(10, 86 * count / maxCount),
                              decoration: BoxDecoration(
                                color: count == 0
                                    ? const Color(0xFFE8E8E8)
                                    : _button,
                                borderRadius: BorderRadius.circular(7),
                              ),
                            ),
                            const SizedBox(height: 6),
                            Text(
                              weekdays[sevenDays[index].weekday - 1],
                              style: const TextStyle(
                                fontSize: 10,
                                color: _faint,
                              ),
                            ),
                          ],
                        ),
                      );
                    }),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({required this.number, required this.label});
  final int number;
  final String label;
  @override
  Widget build(BuildContext context) => Expanded(
    child: Container(
      padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 6),
      decoration: BoxDecoration(
        color: _surface,
        borderRadius: BorderRadius.circular(_radiusMd),
      ),
      child: Column(
        children: [
          Text(
            '$number',
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w600,
              letterSpacing: -.5,
            ),
          ),
          const SizedBox(height: 2),
          Text(label, style: const TextStyle(fontSize: 11, color: _muted)),
        ],
      ),
    ),
  );
}

class _SettingsPage extends StatelessWidget {
  const _SettingsPage({
    required this.providers,
    required this.onBack,
    required this.onEditProvider,
    required this.onToast,
  });
  final List<ApiProvider> providers;
  final VoidCallback onBack;
  final ValueChanged<ApiProvider?> onEditProvider;
  final ValueChanged<String> onToast;
  @override
  Widget build(BuildContext context) => Column(
    children: [
      _TopHeader(
        left: _BackButton(onTap: onBack),
        center: const Text(
          '设置',
          style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
        ),
      ),
      Expanded(
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(22, 8, 22, 60),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _SettingsCard(
                children: [
                  _SettingsProfile(onTap: () => onToast('演示版本，暂不支持编辑资料')),
                ],
              ),
              const _SectionTitle('API 配置'),
              _SettingsCard(
                children: [
                  ...providers.map(
                    (provider) => _SettingsItem(
                      iconName: 'database',
                      label: provider.name,
                      value:
                          '${formatLabels[provider.format] ?? provider.format} · ${provider.models.length} 模型',
                      onTap: () => onEditProvider(provider),
                    ),
                  ),
                  _SettingsItem(
                    iconName: 'plus',
                    label: '添加供应商',
                    muted: true,
                    showChevron: false,
                    onTap: () => onEditProvider(null),
                  ),
                ],
              ),
              const _SectionTitle('通用'),
              _SettingsCard(
                children: [
                  _SettingsItem(
                    iconName: 'bell',
                    label: '通知',
                    onTap: () => onToast('演示版本，敬请期待'),
                  ),
                  _SettingsItem(
                    iconName: 'shield',
                    label: '隐私',
                    onTap: () => onToast('演示版本，敬请期待'),
                  ),
                  _SettingsItem(
                    iconName: 'info',
                    label: '关于 Archive',
                    value: 'v1.0.0',
                    onTap: () => onToast('Archive v1.0.0'),
                  ),
                ],
              ),
              _SettingsCard(
                children: [
                  _SettingsItem(
                    iconName: 'logout',
                    label: '退出登录',
                    dangerous: true,
                    showChevron: false,
                    onTap: () => onToast('演示版本，不会真的退出'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    ],
  );
}

class _SettingsCard extends StatelessWidget {
  const _SettingsCard({required this.children});
  final List<Widget> children;
  @override
  Widget build(BuildContext context) => Container(
    margin: const EdgeInsets.only(bottom: 12),
    decoration: BoxDecoration(
      color: _surface,
      borderRadius: BorderRadius.circular(_radiusMd),
    ),
    clipBehavior: Clip.antiAlias,
    child: Column(children: children),
  );
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.title);
  final String title;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(top: 10, bottom: 12),
    child: Text(
      title,
      style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
    ),
  );
}

class _SettingsProfile extends StatelessWidget {
  const _SettingsProfile({required this.onTap});
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => InkWell(
    onTap: onTap,
    child: const Padding(
      padding: EdgeInsets.all(16),
      child: Row(
        children: [
          _Avatar(size: 46),
          SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Serein',
                  style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
                ),
                SizedBox(height: 1),
                Text('编辑个人资料', style: TextStyle(fontSize: 11, color: _faint)),
              ],
            ),
          ),
          ArchiveIcon('chevron-right', size: 18, color: _faint),
        ],
      ),
    ),
  );
}

class _SettingsItem extends StatelessWidget {
  const _SettingsItem({
    required this.iconName,
    required this.label,
    required this.onTap,
    this.value,
    this.muted = false,
    this.dangerous = false,
    this.showChevron = true,
  });
  final String iconName;
  final String label;
  final VoidCallback onTap;
  final String? value;
  final bool muted;
  final bool dangerous;
  final bool showChevron;
  @override
  Widget build(BuildContext context) {
    final color = dangerous ? _danger : (muted ? _muted : _ink);
    return InkWell(
      onTap: onTap,
      child: Container(
        height: 61,
        padding: const EdgeInsets.symmetric(horizontal: 16),
        decoration: const BoxDecoration(
          border: Border(top: BorderSide(color: Color(0xFFF0F0F0))),
        ),
        child: Row(
          children: [
            Container(
              width: 32,
              height: 32,
              decoration: const BoxDecoration(
                color: _bg,
                borderRadius: BorderRadius.all(Radius.circular(11)),
              ),
              child: ArchiveIcon(iconName, size: 16, color: color),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                label,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  fontSize: 14,
                  color: color,
                  fontWeight: muted ? FontWeight.w400 : FontWeight.w500,
                ),
              ),
            ),
            if (value != null)
              Text(
                value!,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(fontSize: 11, color: _faint),
              ),
            if (showChevron)
              const ArchiveIcon('chevron-right', size: 18, color: _faint),
          ],
        ),
      ),
    );
  }
}

class _ApiEditorPage extends StatefulWidget {
  const _ApiEditorPage({
    super.key,
    required this.provider,
    required this.isNew,
    required this.onBack,
    required this.onSave,
    required this.onDelete,
    required this.onToast,
  });
  final ApiProvider provider;
  final bool isNew;
  final VoidCallback onBack;
  final ValueChanged<ApiProvider> onSave;
  final ValueChanged<ApiProvider> onDelete;
  final ValueChanged<String> onToast;
  @override
  State<_ApiEditorPage> createState() => _ApiEditorPageState();
}

class _ApiEditorPageState extends State<_ApiEditorPage> {
  late ApiProvider _draft;
  late TextEditingController _name;
  late TextEditingController _url;
  late TextEditingController _key;
  final _customModel = TextEditingController();
  final _filter = TextEditingController();
  List<String> _fetched = [];
  bool _loading = false;
  @override
  void initState() {
    super.initState();
    _draft = widget.provider.copy();
    _name = TextEditingController(text: _draft.name);
    _url = TextEditingController(text: _draft.url);
    _key = TextEditingController(text: _draft.key);
    _filter.addListener(() => setState(() {}));
  }

  @override
  void dispose() {
    _name.dispose();
    _url.dispose();
    _key.dispose();
    _customModel.dispose();
    _filter.dispose();
    super.dispose();
  }

  bool _hasModel(String id) => _draft.models.any((item) => item.id == id);
  void _syncDraft() {
    _draft.name = _name.text.trim();
    _draft.url = _url.text.trim();
    _draft.key = _key.text.trim();
  }

  Future<void> _fetchModels() async {
    _syncDraft();
    if (_draft.url.isEmpty) {
      widget.onToast('请先填写 API URL');
      return;
    }
    setState(() => _loading = true);
    widget.onToast('正在获取模型列表…');
    try {
      final endpoint = '${_draft.url.replaceFirst(RegExp(r'/+$'), '')}/models';
      final headers = _draft.format == 'anthropic'
          ? {'x-api-key': _draft.key, 'anthropic-version': '2023-06-01'}
          : {'Authorization': 'Bearer ${_draft.key}'};
      final response = await http
          .get(Uri.parse(endpoint), headers: headers)
          .timeout(const Duration(seconds: 15));
      if (response.statusCode < 200 || response.statusCode >= 300) {
        throw Exception('HTTP ${response.statusCode}');
      }
      final decoded = jsonDecode(response.body) as Map<String, dynamic>;
      final raw = (decoded['data'] ?? decoded['models'] ?? []) as List<dynamic>;
      final models =
          raw
              .map(
                (item) => item is String
                    ? item
                    : (item is Map<String, dynamic>
                          ? item['id']?.toString()
                          : null),
              )
              .whereType<String>()
              .where((item) => item.isNotEmpty)
              .toSet()
              .toList()
            ..sort();
      if (models.isEmpty) throw Exception('无可用模型');
      if (!mounted) {
        return;
      }
      setState(() => _fetched = models);
      widget.onToast('获取到 ${models.length} 个模型');
    } catch (error) {
      widget.onToast(
        '获取失败：${error.toString().replaceFirst('Exception: ', '')}',
      );
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _toggleModel(String id) => setState(() {
    if (_hasModel(id)) {
      _draft.models.removeWhere((item) => item.id == id);
    } else {
      _draft.models.add(ApiModelConfig(id: id));
    }
  });
  void _addCustomModel() {
    final id = _customModel.text.trim();
    if (id.isEmpty) {
      widget.onToast('请输入模型 ID');
      return;
    }
    if (_hasModel(id)) {
      widget.onToast('该模型已存在');
      return;
    }
    setState(() {
      _draft.models.add(ApiModelConfig(id: id));
      _customModel.clear();
    });
  }

  void _save() {
    _syncDraft();
    if (_draft.name.isEmpty) {
      widget.onToast('请填写供应商名称');
      return;
    }
    if (_draft.url.isEmpty) {
      widget.onToast('请填写 API URL');
      return;
    }
    widget.onSave(_draft);
  }

  @override
  Widget build(BuildContext context) {
    final filtered = _fetched
        .where(
          (item) =>
              item.toLowerCase().contains(_filter.text.trim().toLowerCase()),
        )
        .toList();
    return Column(
      children: [
        _TopHeader(
          left: _BackButton(onTap: widget.onBack),
          center: const Text(
            '供应商',
            style: TextStyle(fontSize: 15, fontWeight: FontWeight.w600),
          ),
          right: TextButton(
            onPressed: _save,
            child: const Text(
              '保存',
              style: TextStyle(
                fontSize: 14,
                color: _ink,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ),
        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.fromLTRB(22, 8, 22, 40),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const _FormLabel('供应商名称'),
                _FormField(controller: _name, hint: '自定义名称，如 官方 / 中转站'),
                const SizedBox(height: 16),
                const _FormLabel('API 格式'),
                Container(
                  decoration: BoxDecoration(
                    color: _surface,
                    borderRadius: BorderRadius.circular(14),
                  ),
                  padding: const EdgeInsets.all(4),
                  child: Row(
                    children: ['anthropic', 'openai', 'responses']
                        .map(
                          (format) => Expanded(
                            child: InkWell(
                              onTap: () =>
                                  setState(() => _draft.format = format),
                              borderRadius: BorderRadius.circular(11),
                              child: AnimatedContainer(
                                duration: const Duration(milliseconds: 150),
                                padding: const EdgeInsets.symmetric(
                                  vertical: 9,
                                ),
                                decoration: BoxDecoration(
                                  color: _draft.format == format
                                      ? _button
                                      : Colors.transparent,
                                  borderRadius: BorderRadius.circular(11),
                                ),
                                child: Text(
                                  formatLabels[format]!,
                                  textAlign: TextAlign.center,
                                  style: TextStyle(
                                    fontSize: 12,
                                    color: _draft.format == format
                                        ? _bg
                                        : _muted,
                                    fontWeight: _draft.format == format
                                        ? FontWeight.w500
                                        : null,
                                  ),
                                ),
                              ),
                            ),
                          ),
                        )
                        .toList(),
                  ),
                ),
                const SizedBox(height: 16),
                const _FormLabel('API URL'),
                _FormField(
                  controller: _url,
                  hint: _draft.format == 'anthropic'
                      ? 'https://api.anthropic.com/v1'
                      : 'https://api.openai.com/v1',
                  keyboardType: TextInputType.url,
                ),
                const SizedBox(height: 16),
                const _FormLabel('API Key'),
                _FormField(controller: _key, hint: 'sk-…', obscure: true),
                const SizedBox(height: 22),
                OutlinedButton.icon(
                  onPressed: _loading ? null : _fetchModels,
                  style: OutlinedButton.styleFrom(
                    minimumSize: const Size.fromHeight(46),
                    foregroundColor: _muted,
                    side: const BorderSide(
                      color: Color(0xFFDFDFDF),
                      width: 1.5,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                  ),
                  icon: _loading
                      ? const SizedBox(
                          width: 14,
                          height: 14,
                          child: CircularProgressIndicator(
                            strokeWidth: 1.8,
                            color: _muted,
                          ),
                        )
                      : const ArchiveIcon('refresh', size: 16),
                  label: const Text(
                    '通过 models 接口获取可用模型',
                    style: TextStyle(fontSize: 13),
                  ),
                ),
                if (_fetched.isNotEmpty) ...[
                  _SectionTitle(
                    '可用模型 · ${filtered.length} / ${_fetched.length}',
                  ),
                  TextField(
                    controller: _filter,
                    style: const TextStyle(fontSize: 12),
                    decoration: InputDecoration(
                      hintText: '搜索模型…',
                      hintStyle: const TextStyle(color: _faint),
                      filled: true,
                      fillColor: _surface,
                      isDense: true,
                      contentPadding: const EdgeInsets.symmetric(
                        horizontal: 16,
                        vertical: 10,
                      ),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular(999),
                        borderSide: BorderSide.none,
                      ),
                    ),
                  ),
                  const SizedBox(height: 10),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: filtered
                        .map(
                          (id) => FilterChip(
                            selected: _hasModel(id),
                            onSelected: (_) => _toggleModel(id),
                            showCheckmark: true,
                            label: Text(
                              id,
                              style: const TextStyle(fontSize: 12),
                            ),
                            labelStyle: TextStyle(
                              color: _hasModel(id) ? _bg : _muted,
                            ),
                            selectedColor: _button,
                            backgroundColor: _surface,
                            side: BorderSide.none,
                            shape: const StadiumBorder(),
                            padding: const EdgeInsets.symmetric(
                              horizontal: 5,
                              vertical: 4,
                            ),
                          ),
                        )
                        .toList(),
                  ),
                ],
                _SectionTitle(
                  _draft.models.isEmpty
                      ? '已配置模型'
                      : '已配置模型 · ${_draft.models.length}',
                ),
                if (_draft.models.isEmpty)
                  const Padding(
                    padding: EdgeInsets.only(bottom: 10),
                    child: Center(
                      child: Text(
                        '尚未配置模型，点选上方可用模型或手动添加',
                        style: TextStyle(fontSize: 12, color: _faint),
                      ),
                    ),
                  )
                else
                  ...List.generate(
                    _draft.models.length,
                    (index) => _ModelCard(
                      model: _draft.models[index],
                      onChanged: () => setState(() {}),
                      onDelete: () =>
                          setState(() => _draft.models.removeAt(index)),
                    ),
                  ),
                Row(
                  children: [
                    Expanded(
                      child: _FormField(
                        controller: _customModel,
                        hint: '手动输入模型 ID，如 gpt-4o',
                        onSubmitted: (_) => _addCustomModel(),
                      ),
                    ),
                    const SizedBox(width: 8),
                    _CircularIconButton(
                      iconName: 'plus',
                      onTap: _addCustomModel,
                      dark: true,
                      glass: false,
                      size: 42,
                    ),
                  ],
                ),
                if (!widget.isNew)
                  Padding(
                    padding: const EdgeInsets.only(top: 22),
                    child: SizedBox(
                      width: double.infinity,
                      child: TextButton(
                        onPressed: () => widget.onDelete(_draft),
                        style: TextButton.styleFrom(
                          backgroundColor: _surface,
                          foregroundColor: _danger,
                          padding: const EdgeInsets.symmetric(vertical: 13),
                        ),
                        child: const Text(
                          '删除该供应商',
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _FormLabel extends StatelessWidget {
  const _FormLabel(this.label);
  final String label;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 6),
    child: Text(
      label,
      style: const TextStyle(
        fontSize: 12,
        color: _muted,
        fontWeight: FontWeight.w500,
      ),
    ),
  );
}

class _FormField extends StatelessWidget {
  const _FormField({
    required this.controller,
    required this.hint,
    this.obscure = false,
    this.keyboardType,
    this.onSubmitted,
  });
  final TextEditingController controller;
  final String hint;
  final bool obscure;
  final TextInputType? keyboardType;
  final ValueChanged<String>? onSubmitted;
  @override
  Widget build(BuildContext context) => TextField(
    controller: controller,
    obscureText: obscure,
    keyboardType: keyboardType,
    onSubmitted: onSubmitted,
    style: const TextStyle(fontSize: 13),
    decoration: InputDecoration(
      hintText: hint,
      hintStyle: const TextStyle(color: _faint),
      filled: true,
      fillColor: _surface,
      isDense: true,
      contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 13),
      border: OutlineInputBorder(
        borderRadius: BorderRadius.circular(14),
        borderSide: BorderSide.none,
      ),
    ),
  );
}

class _ModelCard extends StatefulWidget {
  const _ModelCard({
    required this.model,
    required this.onChanged,
    required this.onDelete,
  });
  final ApiModelConfig model;
  final VoidCallback onChanged;
  final VoidCallback onDelete;
  @override
  State<_ModelCard> createState() => _ModelCardState();
}

class _ModelCardState extends State<_ModelCard> {
  late TextEditingController _displayName;
  @override
  void initState() {
    super.initState();
    _displayName = TextEditingController(text: widget.model.displayName);
  }

  @override
  void dispose() {
    _displayName.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Container(
    margin: const EdgeInsets.only(bottom: 10),
    padding: const EdgeInsets.all(14),
    decoration: BoxDecoration(
      color: _surface,
      borderRadius: BorderRadius.circular(18),
    ),
    child: Column(
      children: [
        Row(
          children: [
            Container(
              width: 30,
              height: 30,
              decoration: const BoxDecoration(
                color: _bg,
                borderRadius: BorderRadius.all(Radius.circular(10)),
              ),
              child: const ArchiveIcon('cpu', size: 15),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                widget.model.id,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            const Text('识图', style: TextStyle(fontSize: 11, color: _muted)),
            const SizedBox(width: 6),
            Switch(
              value: widget.model.vision,
              onChanged: (value) {
                setState(() => widget.model.vision = value);
                widget.onChanged();
              },
              activeTrackColor: _button,
              materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            IconButton(
              onPressed: widget.onDelete,
              icon: const ArchiveIcon('close', color: _faint, size: 18),
              splashRadius: 17,
            ),
          ],
        ),
        Padding(
          padding: const EdgeInsets.only(left: 40, top: 7),
          child: TextField(
            controller: _displayName,
            onChanged: (value) {
              widget.model.displayName = value;
              widget.onChanged();
            },
            style: const TextStyle(fontSize: 12, color: _muted),
            decoration: const InputDecoration(
              hintText: '自定义显示名（可选）',
              hintStyle: TextStyle(fontSize: 12, color: _faint),
              isDense: true,
              contentPadding: EdgeInsets.only(bottom: 4),
              enabledBorder: UnderlineInputBorder(
                borderSide: BorderSide(color: Color(0xFFE2E2E2)),
              ),
              focusedBorder: UnderlineInputBorder(
                borderSide: BorderSide(color: _muted),
              ),
            ),
          ),
        ),
      ],
    ),
  );
}

class _SideDrawer extends StatefulWidget {
  const _SideDrawer({
    required this.visible,
    required this.onClose,
    required this.onSelect,
  });
  final bool visible;
  final VoidCallback onClose;
  final ValueChanged<AppPage> onSelect;

  @override
  State<_SideDrawer> createState() => _SideDrawerState();
}

class _SideDrawerState extends State<_SideDrawer> {
  double _progress = 0;
  bool _dragging = false;

  @override
  void didUpdateWidget(covariant _SideDrawer oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.visible != widget.visible && !_dragging) {
      _progress = widget.visible ? 1 : 0;
    }
  }

  void _select(AppPage page) {
    widget.onClose();
    Future<void>.delayed(const Duration(milliseconds: 120), () {
      if (mounted) widget.onSelect(page);
    });
  }

  void _dragUpdate(DragUpdateDetails details, double width) {
    setState(() {
      _dragging = true;
      _progress = (_progress + (details.primaryDelta ?? 0) / width).clamp(
        0.0,
        1.0,
      );
    });
  }

  void _dragEnd(DragEndDetails details) {
    final velocity = details.primaryVelocity ?? 0;
    final shouldClose = _progress < .5 || velocity < -760;
    setState(() {
      _dragging = false;
      _progress = shouldClose ? 0 : 1;
    });
    if (shouldClose) widget.onClose();
  }

  @override
  Widget build(BuildContext context) {
    final width = min(MediaQuery.sizeOf(context).width * .8, 320.0);
    final progress = widget.visible ? _progress : 0.0;
    return IgnorePointer(
      ignoring: !widget.visible && !_dragging,
      child: Stack(
        children: [
          AnimatedOpacity(
            opacity: progress,
            duration: _dragging
                ? Duration.zero
                : const Duration(milliseconds: 250),
            child: GestureDetector(
              onTap: widget.onClose,
              child: const ColoredBox(
                color: Color(0x4D000000),
                child: SizedBox.expand(),
              ),
            ),
          ),
          AnimatedContainer(
            duration: _dragging
                ? Duration.zero
                : const Duration(milliseconds: 300),
            curve: Curves.easeOutCubic,
            transform: Matrix4.translationValues(-width * (1 - progress), 0, 0),
            child: GestureDetector(
              onHorizontalDragUpdate: (details) => _dragUpdate(details, width),
              onHorizontalDragEnd: _dragEnd,
              child: Container(
                width: width,
                height: double.infinity,
                padding: const EdgeInsets.fromLTRB(0, 28, 0, 16),
                decoration: const BoxDecoration(
                  color: _bg,
                  borderRadius: BorderRadius.horizontal(
                    right: Radius.circular(_radiusLg),
                  ),
                ),
                child: Column(
                  children: [
                    const Padding(
                      padding: EdgeInsets.fromLTRB(22, 0, 22, 22),
                      child: Row(
                        children: [
                          _Avatar(size: 48),
                          SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Serein',
                                  style: TextStyle(
                                    fontSize: 16,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                                SizedBox(height: 1),
                                Text(
                                  'Record · Reflect · Remember',
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                  style: TextStyle(
                                    fontSize: 11,
                                    color: _faint,
                                    letterSpacing: .3,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    Expanded(
                      child: Column(
                        children: [
                          _DrawerItem(
                            iconName: 'achi',
                            label: 'Achi',
                            ai: true,
                            onTap: () => _select(AppPage.achi),
                          ),
                          _DrawerItem(
                            iconName: 'image',
                            label: '相册',
                            onTap: () => _select(AppPage.album),
                          ),
                          _DrawerItem(
                            iconName: 'chart',
                            label: '统计',
                            onTap: () => _select(AppPage.stats),
                          ),
                          const Spacer(),
                          const Divider(height: 1, color: _surface),
                          _DrawerItem(
                            iconName: 'settings',
                            label: '设置',
                            onTap: () => _select(AppPage.settings),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _DrawerItem extends StatelessWidget {
  const _DrawerItem({
    required this.iconName,
    required this.label,
    required this.onTap,
    this.ai = false,
  });
  final String iconName;
  final String label;
  final VoidCallback onTap;
  final bool ai;
  @override
  Widget build(BuildContext context) => InkWell(
    onTap: () {
      HapticFeedback.selectionClick();
      onTap();
    },
    child: Padding(
      padding: const EdgeInsets.symmetric(horizontal: 22, vertical: 14),
      child: Row(
        children: [
          ArchiveIcon(iconName, size: 18, color: _muted),
          const SizedBox(width: 13),
          Expanded(
            child: Text(
              label,
              style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500),
            ),
          ),
          if (ai) const _AiTag(),
          const ArchiveIcon('chevron-right', size: 17, color: _faint),
        ],
      ),
    ),
  );
}
