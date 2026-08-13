# Archive Flutter

这是对所附 HTML 原型进行 Flutter 原生实现的项目。应用保留了原型的极简黑白视觉系统、移动端最大内容宽度、圆角卡片与抽屉/底部选择器等交互，并将网页端的内存交互迁移为 Flutter 组件状态与本地配置持久化。

## 已实现范围

| 区域 | 已实现功能 |
|---|---|
| 首页时间流 | 日期栏、文字摘要、单图/多图布局、入场过渡动画，以及进入记录详情的交互。 |
| 新建记录 | 文本输入、多图选择和删除、地点/天气/心情底部选择器、提交状态控制，以及即时回流至首页。 |
| 记录详情 | 完整正文、图片网格、日期元数据、地点/天气/心情信息，以及评论和二级回复。 |
| 侧边抽屉 | Achi、相册、统计和设置入口，以及背景遮罩和滑入动画。 |
| Achi | 对话气泡、发送状态控制、自动滚动和模拟延迟回复。 |
| 相册与统计 | 照片汇总网格、关联记录跳转、记录/照片/评论计数，以及最近 7 天柱状图。 |
| API 配置 | 供应商增删改、Anthropic/OpenAI/Responses 格式切换、模型拉取、搜索、点选、手动添加、视觉识别开关与显示名编辑。 |
| 本地保存 | API 供应商和模型配置通过 `shared_preferences` 保存；运行期创建的记录与本地选图保持当前会话可用。 |
| 原型图标与资源 | HTML 同源 SVG 路径统一由 `ArchiveIcon` 渲染；示例头像和四张记录图片已打包为离线资源，网络图片使用缓存与渐显回退。 |
| 手势与动效 | 支持 Android 预测性返回回调、左缘右滑返回/打开抽屉、抽屉拖拽跟手与速度甩动关闭；页面前进和返回采用双向滑入淡入转场。 |
| 交互性能 | 图片内存缓存、按显示尺寸解码、首屏资源预热、列表键值复用、隔离重绘与轻触触觉反馈。 |

> API 模型拉取会向用户配置的 `API URL/models` 发送请求。Web 端是否成功取决于目标服务的 CORS 策略；移动端不受浏览器 CORS 限制。

## 依赖

项目使用 `image_picker` 完成相册选图，`shared_preferences` 保存供应商配置，`http` 请求模型列表，`flutter_svg` 渲染 HTML 同源 SVG 图标，`cached_network_image` 缓存用户后续添加的网络图片。Android 已声明网络访问权限，iOS 已配置照片库访问说明。

## 运行方法

请在已安装 Flutter SDK 的环境中执行以下命令：

```bash
flutter pub get
flutter run
```

若需运行测试和构建 Web 发布物：

```bash
flutter analyze
flutter test
flutter build web --release
```

构建完成后，Web 静态产物位于 `build/web/`。项目同时保留 Android 与 iOS 平台目录，可在配有相应原生工具链的设备环境中构建。

## 验证记录

| 检查项 | 结果 |
|---|---|
| `dart format lib test` | 通过 |
| `flutter analyze` | 通过，未发现问题 |
| `flutter test` | 通过，包含首页启动烟雾测试 |
| `flutter build web --release` | 通过，产物位于 `build/web/` |
| `flutter build apk --release --build-name=1.1.1 --build-number=3` | 通过，产物位于 `build/app/outputs/flutter-apk/app-release.apk` |
| APK 签名校验 | 通过 APK Signature Scheme v2 校验 |

本次新增 HTML 同源 SVG 图标渲染、离线示例图片资源、图片缓存与渐显回退、Android 预测性返回回调、左缘滑动、抽屉跟手拖拽、双向页面转场和轻触触觉反馈。v1.1.1 进一步为 SVG 图标补充固定尺寸外框、光学缩放和最大尺寸约束，并使用带方向和序列号的稳定路由令牌，仅在真实导航时触发裁切式位移动画，从而消除无关状态更新导致的闪烁。已配置 Android SDK 36、Build Tools 36.0.0 和 Java 17，生成的 v1.1.1 安装包最低支持 Android 7.0（API 24），目标版本为 Android 16（API 36）。APK 使用 Android 调试证书签名，适合直接安装测试；如需发布到应用商店，请使用您自己的发布密钥重新签名。

## v1.2.0 升级与体积优化

本次升级使用 [`liquid_glass_widgets`](https://pub.dev/packages/liquid_glass_widgets) 为主页新建、发布确认、评论发送、Achi 发送、模型添加和供应商保存等主要操作提供液态玻璃表面、光晕、弹性按压和触觉反馈。应用启动阶段会预热相关着色器；页面切换改为短距离滑移配合细微缩放，保留反向导航方向，且只在真实路由变化时播放，避免状态更新时发生闪烁。

用户提供的黑色羽毛图案已配置为 Android 的多分辨率自适应启动图标，清单引用为 `@mipmap/launcher_icon`。示例记录图片已由 PNG 转为质量 88 的 WebP，Android release 同时启用 R8 代码压缩和未使用资源剔除。按照 Flutter Agent Plugins 中的组件测试与静态分析工作流，项目已完成 `dart fix --dry-run`、`flutter analyze --fatal-infos` 与液态玻璃首页按钮组件测试。

| 交付 APK | 适用设备 | 体积 | 相较 v1.1.1 通用 APK |
|---|---:|---:|---:|
| `app-arm64-v8a-release.apk` | 大多数近年的真机，推荐 | 20.0 MB | 减少 67.00% |
| `app-armeabi-v7a-release.apk` | 32 位 ARM 旧设备 | 17.5 MB | 按 ABI 拆分交付 |
| `app-x86_64-release.apk` | 64 位 Android 模拟器 | 21.5 MB | 按 ABI 拆分交付 |

> 建议普通 Android 手机优先安装 arm64-v8a 版本。三个 APK 均为 v1.2.0（构建号 4），最低支持 Android 7.0（API 24），并已通过 APK Signature Scheme v2 校验。测试构建使用 Android 调试证书签名；上架前请改用发布密钥。

## 参考资料

[1]: https://pub.dev/packages/liquid_glass_widgets "liquid_glass_widgets"
[2]: https://github.com/flutter/agent-plugins "Flutter Agent Plugins"

## v1.2.1 可读性与转场修复

此版本移除了先前对 `LiquidGlassWidgets.wrap()` 注入的全局厚玻璃主题，恢复组件库快速开始示例的默认全局初始化方式。主页、记录编辑、评论和 Achi 等内容页的圆形主操作控件使用 `GlassButtonStyle.transparent` 与官方工具栏示例中的 `stretch: 0.15`，仅保留按压时的液态拉伸与高光反馈，不再在白色页面上铺设不透明的白色玻璃圆面，也没有额外添加阴影。

页面切换改为 260ms 的单层滑移动画；切换时仅保留进入页面的绘制层，主动丢弃离场页面，避免下一页已经显示时仍保留上一页画面。设置与供应商编辑流程已恢复为原有的纯净控件样式，不应用液态玻璃。v1.2.1（构建号 5）已通过 `flutter analyze --fatal-infos`、组件测试与 APK v2 签名验证。
