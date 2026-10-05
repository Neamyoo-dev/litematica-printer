# 打印机四改

[English](README.md) | **简体中文**

![Minecraft](https://img.shields.io/badge/Minecraft-26.2-blue)

为 [Litematica](https://modrinth.com/mod/litematica) 投影添加自动建造功能的 Minecraft Fabric 模组。本项目仅支持并更新 **Minecraft 26.2**，默认开发分支为 `ver/26.2`。

基于 Litematica Printer 的历次修改版本继续维护，重点修复创造模式漏放、点击位置偏移和扫描范围更新问题。项目保持开源免费。

## 下载与前置

在本项目的 [Releases](https://github.com/Neamyoo-dev/litematica-printer/releases) 下载适用于 26.2 的构建，或自行编译。

必需模组：

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [MaLiLib](https://modrinth.com/mod/malilib)
- [Litematica](https://modrinth.com/mod/litematica)

可选模组：

- [Tweakeroo](https://modrinth.com/mod/tweakeroo)：挖掘限制名单、自动选择工具。
- [Quick Shulker](https://modrinth.com/mod/quick-shulker) 或 AxShulkers：快捷潜影盒取料。
- [Fabric-Bedrock-Miner](https://github.com/bunnyi116/fabric-bedrock-miner)：破基岩支持。
- Remote Inventory Next：远程容器取料与物品回塞。

所有前置与可选模组均需使用兼容 26.2 的版本。

## 功能简介

- 自动打印投影，调整方块状态，清理错误及多余方块。
- 选区填充，可先破坏阻挡方块再填充；另有流体移除、农作物催熟、破基岩。
- 数据包打印模式、延迟检测、48 种范围迭代方式。
- 工作进度显示、缺失材料提示、处理中的方块高亮。
- 快捷潜影盒及远程容器自动取料，使用后按顺序回塞物品。
- 楼梯、门、活版门、漏斗、旗帜、头颅等特殊方块的朝向处理。
- 简体中文、繁体中文、文言文、英语、俄语界面。

## 使用方法

1. 在游戏中加载原理图，并启用需要打印的投影和子区域。
2. 移动到目标方块的交互范围内。
3. 按 `Caps Lock` 开启打印机。
4. 在打印机设置中按需要调整速度、工作范围和可见层。

大部分设置都有游戏内说明。创造模式会自动取物；没有配置投影取物槽位时，使用当前选中的快捷栏槽位。

填充选区时，在「填充」设置中开启 **破坏阻挡方块**（默认关闭），即可先清除阻挡再放入填充方块。创造模式会瞬间破坏黑曜石等可破坏方块，速度由放置间隔和每刻放置数量控制；生存模式按正常挖掘速度处理，挖完自动恢复填充材料。已有填充列表中的方块种类会保留，避免反复拆建。

此选项仅在使用方块物品且有可用材料时执行，遵循选区、工作范围和破坏限制名单。数据包破坏模式会等待世界中的阻挡方块消失后再填充；服务器仍可拒绝受保护位置的破坏或放置。

## 蓝色缺失方块为什么没有填上？

蓝色表示投影与世界存在缺失差异，仍需满足工作范围、图层、子区域和方块放置条件。创造模式也受服务端放置规则影响。

本分支的修复覆盖以下问题：

- 凭空放置以前直接点击目标坐标，忽略现有支撑面；现在优先点击真实支撑，仅在没有支撑且允许凭空放置时回退。
- 点击偏移旋转把角度当作弧度，可能点到错误位置；现在使用正确的弧度。
- 复用扫描范围时玩家眼睛位置没有同步刷新；现在每刻更新可达性判断。
- 创造模式没有配置投影取物槽位时无法自动取物；现在可使用当前快捷栏槽位。
- 开启破冰放水时，创造模式会跳过需要水的方块；现在创造模式使用正常的水桶放置流程。

如果仍有漏填，请检查：

- 打印开关、跳过名单、跳过含水方块、投影子区域及可见层设置。
- 火把、植物、红石等是否有实际支撑；沙子、砂砾等下落方块下方是否已经建好。
- 放置位置是否与玩家或实体碰撞，是否受服务器领地保护、反作弊或放置速率限制。
- 尝试每刻放置 1 个方块，适当增大工作间隔，并靠近缺失位置。服务端限制不能靠创造模式或增大客户端工作范围绕过。

尚未支持：装有液体的炼药锅、睡莲、物品展示框/盔甲架/画等实体，以及部分非原版内容。

反馈时请在本项目的 [Issues](https://github.com/Neamyoo-dev/litematica-printer/issues) 提供方块名称、打印设置，以及单人/服务器环境。

## 编译

使用 **JDK 25**。构建会从公开发布包取得 26.2 的远程容器依赖，无需上游 GitHub Packages 认证。

```bash
./gradlew :26.2:build
```

Windows 使用：

```powershell
.\gradlew.bat :26.2:build
```

产物位于 `versions/26.2/build/libs/`，将其中的普通模组 jar 放入游戏的 `mods` 目录。构建仅生成 26.2 版本，不再生成多版本整合包。

## 贡献

仓库工作约定见 [AGENTS.md](AGENTS.md)。新增提交使用英文 [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)；文档更新需同时维护英文和简体中文 README。

## 特别感谢

- [aleksilassila](https://github.com/aleksilassila/litematica-printer)：原始 Litematica Printer。
- [zhaixianyu（宅咸鱼）](https://github.com/zhaixianyu/litematica-printer)：二改版本。
- [BiliXWhite / BlinkWhite](https://github.com/BiliXWhite/litematica-printer)：三改版本与功能扩展。
- [bunny_i / bunnyi116](https://github.com/bunnyi116)：开发贡献与破基岩支持。
- [MoRanpcy](https://github.com/MoRanpcy/quickshulker)：快捷潜影盒支持。
- [Rofumer](https://github.com/Rofumer)：俄语本地化、性能优化与错误修复。
- [Cjsah](https://github.com/Cjsah)：选区容器材料识别。
- [EnderPhantomWing](https://github.com/EnderPhantomWing-Fork)：新版本适配与错误修复。

感谢所有提供反馈和参与贡献的朋友。项目沿用 [AGPL-3.0](LICENSE.md) 许可证。
