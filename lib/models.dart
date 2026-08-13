import 'dart:convert';

class ArchiveComment {
  ArchiveComment({
    required this.id,
    required this.name,
    required this.avatar,
    required this.text,
    required this.time,
    List<ArchiveReply>? replies,
  }) : replies = replies ?? [];

  final String id;
  final String name;
  final String avatar;
  final String text;
  final String time;
  final List<ArchiveReply> replies;
}

class ArchiveReply {
  ArchiveReply({
    required this.name,
    required this.avatar,
    required this.text,
    required this.time,
  });

  final String name;
  final String avatar;
  final String text;
  final String time;
}

class ArchiveEntry {
  ArchiveEntry({
    required this.id,
    required this.timestamp,
    required this.text,
    required this.images,
    required this.location,
    required this.weather,
    required this.mood,
    List<ArchiveComment>? comments,
  }) : comments = comments ?? [];

  final String id;
  final DateTime timestamp;
  final String text;
  final List<String> images;
  final String location;
  final String weather;
  final String mood;
  final List<ArchiveComment> comments;
}

class ApiModelConfig {
  ApiModelConfig({
    required this.id,
    this.displayName = '',
    this.vision = false,
  });

  final String id;
  String displayName;
  bool vision;

  Map<String, dynamic> toJson() => {
    'id': id,
    'displayName': displayName,
    'vision': vision,
  };

  factory ApiModelConfig.fromJson(Map<String, dynamic> json) => ApiModelConfig(
    id: json['id'] as String? ?? '',
    displayName: json['displayName'] as String? ?? '',
    vision: json['vision'] as bool? ?? false,
  );
}

class ApiProvider {
  ApiProvider({
    required this.id,
    required this.name,
    required this.format,
    required this.url,
    required this.key,
    List<ApiModelConfig>? models,
  }) : models = models ?? [];

  final String id;
  String name;
  String format;
  String url;
  String key;
  List<ApiModelConfig> models;

  ApiProvider copy() => ApiProvider.fromJson(toJson());

  Map<String, dynamic> toJson() => {
    'id': id,
    'name': name,
    'format': format,
    'url': url,
    'key': key,
    'models': models.map((item) => item.toJson()).toList(),
  };

  factory ApiProvider.fromJson(Map<String, dynamic> json) => ApiProvider(
    id: json['id'] as String? ?? '',
    name: json['name'] as String? ?? '',
    format: json['format'] as String? ?? 'openai',
    url: json['url'] as String? ?? '',
    key: json['key'] as String? ?? '',
    models: ((json['models'] as List<dynamic>?) ?? [])
        .whereType<Map<String, dynamic>>()
        .map(ApiModelConfig.fromJson)
        .toList(),
  );

  static List<ApiProvider> decodeList(String value) {
    final decoded = jsonDecode(value) as List<dynamic>;
    return decoded
        .whereType<Map<String, dynamic>>()
        .map(ApiProvider.fromJson)
        .toList();
  }

  static String encodeList(List<ApiProvider> providers) =>
      jsonEncode(providers.map((item) => item.toJson()).toList());
}

const String avatarMe = 'asset:assets/avatars/serein.svg';
const String avatarJune = 'asset:assets/avatars/june.svg';
const String avatarZed = 'asset:assets/avatars/zed.svg';

List<ArchiveEntry> initialEntries() {
  final now = DateTime.now();
  return [
    ArchiveEntry(
      id: '3',
      timestamp: now.subtract(const Duration(hours: 2)),
      text: '周末的下午在街角咖啡馆。\n没什么特别的事，只是看着窗外发呆，感觉时间变得很慢。阳光刚好打在桌面的杯子上，形成好看的几何阴影。',
      images: const ['asset:assets/images/cafe.webp'],
      location: '上海 · 武康路',
      weather: 'sunny',
      mood: 'calm',
      comments: [
        ArchiveComment(
          id: 'c1',
          name: 'June',
          avatar: avatarJune,
          text: '看起来好惬意，这家店在哪呀？',
          time: '1小时前',
          replies: [
            ArchiveReply(
              name: 'Zed',
              avatar: avatarZed,
              text: '同求！下次一起。',
              time: '30分钟前',
            ),
          ],
        ),
        ArchiveComment(
          id: 'c2',
          name: 'Zed',
          avatar: avatarZed,
          text: '光影那张拍得真好。',
          time: '40分钟前',
        ),
      ],
    ),
    ArchiveEntry(
      id: '2',
      timestamp: now.subtract(const Duration(days: 2)),
      text: '整理房间时翻出了一些旧物。这些不曾想起的片段，构成了现在的我。',
      images: const [
        'asset:assets/images/old_01.webp',
        'asset:assets/images/old_02.webp',
        'asset:assets/images/old_03.webp',
      ],
      location: '',
      weather: 'overcast',
      mood: 'low',
      comments: [
        ArchiveComment(
          id: 'c3',
          name: 'Zed',
          avatar: avatarZed,
          text: '旧物最动人了。',
          time: '1天前',
        ),
      ],
    ),
    ArchiveEntry(
      id: '1',
      timestamp: now.subtract(const Duration(days: 5)),
      text: '决定开始记录。',
      images: const [],
      location: '',
      weather: '',
      mood: '',
    ),
  ];
}

List<ApiProvider> defaultProviders() => [
  ApiProvider(
    id: 'p-demo',
    name: 'Anthropic 官方',
    format: 'anthropic',
    url: 'https://api.anthropic.com/v1',
    key: '',
    models: [
      ApiModelConfig(
        id: 'claude-sonnet-4-5',
        displayName: 'Claude Sonnet',
        vision: true,
      ),
    ],
  ),
];

const Map<String, String> weatherLabels = {
  'sunny': '晴朗',
  'cloudy': '多云',
  'overcast': '阴',
  'rain': '下雨',
  'snow': '下雪',
};

const Map<String, String> moodLabels = {
  'calm': '平静',
  'happy': '开心',
  'energy': '充满能量',
  'low': '有点低落',
  'cozy': '悠闲',
};

const List<String> locations = [
  '上海 · 武康路',
  '北京 · 三里屯',
  '杭州 · 天目里',
  '成都 · 玉林路',
  '广州 · 东山口',
];

const Map<String, String> formatLabels = {
  'anthropic': 'Anthropic',
  'openai': 'OpenAI',
  'responses': 'Responses',
};

String monthShort(int month) => const [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
][month - 1];

String fullDate(DateTime date) =>
    '${date.year}年${date.month}月${date.day}日 · ${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';

int entryCommentCount(ArchiveEntry entry) => entry.comments.fold(
  0,
  (total, comment) => total + 1 + comment.replies.length,
);

String weatherIconName(String key) {
  switch (key) {
    case 'sunny':
      return 'sun';
    case 'cloudy':
      return 'cloud-sun';
    case 'overcast':
      return 'cloud';
    case 'rain':
      return 'cloud-rain';
    case 'snow':
      return 'snowflake';
    default:
      return 'cloud';
  }
}

String moodIconName(String key) {
  switch (key) {
    case 'calm':
      return 'leaf';
    case 'happy':
      return 'smile';
    case 'energy':
      return 'zap';
    case 'low':
      return 'moon';
    case 'cozy':
      return 'coffee';
    default:
      return 'smile';
  }
}
