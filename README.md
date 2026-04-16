# PhoneHelper 演示 APK

这是为“移动样本深析：APK逆向与沙箱研判”课程准备的教学演示工程。

## 设计原则

- UI 看起来像一个正常的“手机优化/清理工具”，避免过于简陋。
- Manifest 中保留过度权限、硬编码外联地址、混淆发布配置，便于静态分析。
- 默认启用安全教学模式：上传的是演示数据与环境摘要，不直接读取真实短信或通讯录内容。
- 网络目标固定为 http://httpbin.org/post，方便用 Fiddler 抓取完整请求和响应。

## 目录说明

- app/src/main/java/com/demo/phonehelper/MainActivity.java：主界面与交互入口
- app/src/main/java/com/demo/phonehelper/data/DataCollector.java：构造教学演示用 JSON 负载
- app/src/main/java/com/demo/phonehelper/data/StorageManager.java：本地缓存 JSON 文件
- app/src/main/java/com/demo/phonehelper/data/NetworkUploader.java：上传到 httpbin

## GitHub 构建

当前工程支持两种推送方式：

- 如果你把“培训”目录作为 GitHub 仓库根目录，使用仓库根目录下的 .github/workflows/build-apk.yml。
- 如果你只把 APK 目录单独作为仓库上传，使用 APK/.github/workflows/build-apk-standalone.yml。

构建产物位置：

- APK/app/build/outputs/apk/debug/

当前 GitHub Actions 默认产出可直接安装的 debug APK，用于课堂演示最稳妥。

如果你从 GitHub Actions 下载 artifact，需要先解压，再安装其中的 app-debug.apk。

## 本地说明

当前工程没有附带 Gradle Wrapper，目的是让你优先走 GitHub Actions 构建，不依赖本地 Android Studio。

## 当前实现说明

- UI 已按“真实工具类应用”方向做成仪表盘式首页，不是只有一个按钮的简陋演示壳。
- 默认会写入本地 JSON 报告，并向 http://httpbin.org/post 发起 POST 请求，方便你在 Fiddler 中直接抓到请求和响应。
- 为保证教学安全，当前实现上传的是演示环境摘要和教学负载，不直接发送真实短信正文或通讯录内容。

