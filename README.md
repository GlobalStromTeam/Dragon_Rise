# DragonRiseReforge / 龙之崛起


## 介绍

“卓越前线"的附属载具包。

## 开发工具（改模型/加护甲先看这里）

- **[tools/README.md](tools/README.md)** —— 全部工具与脚本索引；**护甲流水线**（`.bbmodel`/`.geo.json` → 护甲 geo → 校验 → 图标 → 注册）的入口
- **[docs/护甲模型规范.md](docs/护甲模型规范.md)** —— 护甲数据必须满足的骨骼/枢轴/贴图规范
- **[使用说明.md](使用说明.md)** —— 载具数据 + datagen 生成器体系

常用命令：

```bash
node tools/geo_to_armor.mjs --all "<源目录>" "src/main/resources/assets/dragonrise_reforge/models/bedrock/armor"
python tools/make_armor_icons.py
node tools/gen_armor_set.mjs
./gradlew armorGeoCheck        # 或 armorGeoFix
```

## 安装教程

和卓越前线本体同理，请使用[卓越前线0.8.9](https://www.curseforge.com/minecraft/mc-mods/superb-warfare/files/7292685)

## 协议

本模组的代码部分使用 GPL-3.0 协议进行开源。

模型、贴图等资源，除特殊标注外的，均归制作组所有，禁止在未获得制作组授权的情况下使用。

## 相关链接

QQ群: 828262126

[CurseForge](https://www.curseforge.com/minecraft/mc-mods/sbw-drr)

[Gitee](https://gitee.com/ly01lin/Dragon_Rise)

[Discord](https://discord.gg/zSEfkjwx6U)
