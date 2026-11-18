# Plcmd

一款从`PocketMine`移植的`命令木牌`插件

### 如何编译:

> 不要问为什么没有**Gradle**

```bash
mvn clean package
```
(如果是`Maven daemon`)
```bash
mvnd clean package
```

### 版本区别

`classic` 为扁平化更新前的版本制作的 (1.4后, 1.12.2前)

`legacy` 为1.13+准备的版本

`nuevo` 为1.20+的**bukkit**适配的版本

`nuevo-folia` 专门针对**Folia**谱系准备的版本

### 如何使用

在使用 **告示牌** 或 **悬挂告示牌** 的时候, 第二行输入`plcmd`

随后在第三行输入你想要的命令即可

例如:
```text
给xp
plcmd
xp add @s 100
```