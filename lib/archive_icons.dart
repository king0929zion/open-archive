import 'package:flutter/material.dart';
import 'package:flutter_svg/flutter_svg.dart';

/// Renders the same 24px outline paths used by the supplied HTML prototype.
class ArchiveIcon extends StatelessWidget {
  const ArchiveIcon(
    this.name, {
    super.key,
    this.size = 20,
    this.color = const Color(0xFF111111),
    this.semanticLabel,
  });

  final String name;
  final double size;
  final Color color;
  final String? semanticLabel;

  @override
  Widget build(BuildContext context) {
    final paths = _paths[name];
    final frameSize = size.clamp(8.0, 28.0);
    if (paths == null) {
      return SizedBox.square(dimension: frameSize);
    }
    // Lucide paths are designed on a 24px grid.  Keeping the SVG inside a
    // fixed frame prevents it from inheriting unbounded constraints, while a
    // modest optical reduction matches the original HTML icon weight.
    final glyphSize = frameSize * .82;
    return SizedBox.square(
      dimension: frameSize,
      child: Center(
        child: RepaintBoundary(
          child: ExcludeSemantics(
            excluding: semanticLabel == null,
            child: SvgPicture.string(
              '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" '
              'preserveAspectRatio="xMidYMid meet" stroke="#111111" '
              'stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'
              '$paths</svg>',
              width: glyphSize,
              height: glyphSize,
              fit: BoxFit.contain,
              colorFilter: ColorFilter.mode(color, BlendMode.srcIn),
              semanticsLabel: semanticLabel,
            ),
          ),
        ),
      ),
    );
  }

  static const Map<String, String> _paths = {
    'plus': '<path d="M12 5v14"/><path d="M5 12h14"/>',
    'close': '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>',
    'check': '<path d="M20 6 9 17l-5-5"/>',
    'image':
        '<rect width="18" height="18" x="3" y="3" rx="2" ry="2"/>'
        '<circle cx="9" cy="9" r="2"/>'
        '<path d="m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21"/>',
    'map-pin':
        '<path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/>'
        '<circle cx="12" cy="10" r="3"/>',
    'cloud-sun':
        '<path d="M12 2v2"/><path d="m4.93 4.93 1.41 1.41"/>'
        '<path d="M20 12h2"/><path d="m19.07 4.93-1.41 1.41"/>'
        '<path d="M15.947 12.65a4 4 0 0 0-5.925-4.128"/>'
        '<path d="M13 22H7a5 5 0 1 1 4.9-6H13a3 3 0 0 1 0 6Z"/>',
    'sun':
        '<circle cx="12" cy="12" r="4"/><path d="M12 2v2"/>'
        '<path d="M12 20v2"/><path d="m4.93 4.93 1.41 1.41"/>'
        '<path d="m17.66 17.66 1.41 1.41"/><path d="M2 12h2"/>'
        '<path d="M20 12h2"/><path d="m6.34 17.66-1.41 1.41"/>'
        '<path d="m19.07 4.93-1.41 1.41"/>',
    'cloud': '<path d="M17.5 19H9a7 7 0 1 1 6.71-9h1.79a4.5 4.5 0 1 1 0 9Z"/>',
    'cloud-rain':
        '<path d="M4 14.899A7 7 0 1 1 15.71 8h1.79a4.5 4.5 0 0 1 2.5 8.242"/>'
        '<path d="M16 14v6"/><path d="M8 14v6"/><path d="M12 16v6"/>',
    'snowflake':
        '<line x1="2" x2="22" y1="12" y2="12"/>'
        '<line x1="12" x2="12" y1="2" y2="22"/>'
        '<path d="m20 16-4-4 4-4"/><path d="m4 8 4 4-4 4"/>'
        '<path d="m16 4-4 4-4-4"/><path d="m8 20 4-4 4 4"/>',
    'smile':
        '<circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/>'
        '<line x1="9" x2="9.01" y1="9" y2="9"/>'
        '<line x1="15" x2="15.01" y1="9" y2="9"/>',
    'leaf':
        '<path d="M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z"/>'
        '<path d="M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"/>',
    'zap': '<polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>',
    'moon': '<path d="M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z"/>',
    'coffee':
        '<path d="M17 8h1a4 4 0 1 1 0 8h-1"/>'
        '<path d="M3 8h14v9a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4Z"/>'
        '<line x1="6" x2="6" y1="2" y2="4"/>'
        '<line x1="10" x2="10" y1="2" y2="4"/>'
        '<line x1="14" x2="14" y1="2" y2="4"/>',
    'cpu':
        '<rect width="16" height="16" x="4" y="4" rx="2"/>'
        '<rect width="6" height="6" x="9" y="9" rx="1"/>'
        '<path d="M15 2v2"/><path d="M15 20v2"/><path d="M2 15h2"/>'
        '<path d="M2 9h2"/><path d="M20 15h2"/><path d="M20 9h2"/>'
        '<path d="M9 2v2"/><path d="M9 20v2"/>',
    'achi':
        '<path d="M12.3 3.1c4.8.1 8.6 4 8.6 8.9 0 4.9-4 8.9-8.9 8.9-4.8 0-8.7-3.9-8.9-8.7-.06-2.4.86-4.7 2.5-6.3" stroke-width="2.2"/>'
        '<path d="M5.9 6.2c1.7-1.9 4-3 6.4-3.1" stroke-width="2.2"/>'
        '<path d="M9.3 9.1v3.7" stroke-width="2.6"/>'
        '<path d="M14.7 8.9v3.7" stroke-width="2.6"/>',
    'back': '<path d="m15 18-6-6 6-6"/>',
    'chevron-right': '<path d="m9 18 6-6-6-6"/>',
    'send':
        '<path d="M14.536 21.686a.5.5 0 0 0 .937-.024l6.5-19a.496.496 0 0 0-.635-.635l-19 6.5a.5.5 0 0 0-.024.937l7.93 3.18a2 2 0 0 1 1.112 1.11z"/>'
        '<path d="m21.854 2.147-10.94 10.939"/>',
    'reply':
        '<polyline points="9 14 4 9 9 4"/>'
        '<path d="M20 20v-7a4 4 0 0 0-4-4H4"/>',
    'bell':
        '<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/>'
        '<path d="M10.3 21a1.94 1.94 0 0 1-3.6 0"/>',
    'shield': '<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/>',
    'info': '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
    'logout':
        '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>'
        '<polyline points="16 17 21 12 16 7"/><line x1="21" x2="9" y1="12" y2="12"/>',
    'refresh':
        '<path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/>'
        '<path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/>'
        '<path d="M8 16H3v5"/>',
    'database':
        '<rect width="20" height="8" x="2" y="2" rx="2"/>'
        '<rect width="20" height="8" x="2" y="14" rx="2"/>'
        '<line x1="6" x2="6.01" y1="6" y2="6"/>'
        '<line x1="6" x2="6.01" y1="18" y2="18"/>',
    'chart':
        '<path d="M3 3v16a2 2 0 0 0 2 2h16"/>'
        '<path d="M18 17V9"/><path d="M13 17V5"/><path d="M8 17v-3"/>',
    'settings':
        '<path d="M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.50a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l.43-.25a2 2 0 0 1 1-1.73V4a2 2 0 0 0-2-2z"/>'
        '<circle cx="12" cy="12" r="3"/>',
  };
}
