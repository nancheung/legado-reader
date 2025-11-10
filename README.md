![legado-reader](https://img.shields.io/badge/legado%20reader-v1.0.3-green.svg)
![language top](https://img.shields.io/github/languages/top/nancheung/legado-reader?color=orange)
![github stars](https://img.shields.io/github/stars/nancheung/legado-reader)
![platform](https://img.shields.io/badge/platform-JetBrains%20IDE-lightgrey)
![license](https://img.shields.io/github/license/nancheung/legado-reader)
![GitHub Release Date](https://img.shields.io/github/release-date/nancheung/legado-reader)
![GitHub last commit](https://img.shields.io/github/last-commit/nancheung/legado-reader)

# Legado Reader
> 编码，并阅读。
<!-- Plugin description -->
Legado Reader是 [开源阅读APP](https://github.com/gedoor/legado) 的Jetbrains IDE插件版，旨在随时随地在IDE中进行阅读，为编码过程带来灵感和效率的提升。
<!-- Plugin description end -->



# Legado Reader诞生
> 有一天，我在编码的时候，突然想起了一本书，于是我打开了手机，打开了阅读APP，然后打开了书，然后开始阅读，然后我发现，我在阅读的时候，编码的效率并不高。
> 
> 于是我就想，能不能在编码的时候，随时随地阅读。当这个插件完成后，我发现，我在阅读的时候，编码的效率提高了。
> 
> 因此，我希望，这个插件能够帮助到更多的人，让他们在编码的时候，随时随地阅读，提高编码效率。
 

# 开始安装
## 方式一：使用本插件的自有更新方式
> （依赖github，对网速有要求）

> 1. 打开插件市场窗口：`JetBrains IDE` ->`File` -> `Settings` -> `Plugins` -> `Marketplace`
> 2. 打开管理私服窗口：`⚙齿轮小图标` -> `Manage Plugin Repositories...`
> 3. 添加私服文件：`+ Add按钮` -> `Add` -> 
>    `https://raw.githubusercontent.com/nancheung/legado-reader/master/updatePlugins.xml`
> 4. 安装插件：`Plugins` -> `Marketplace`，搜索：`Legado Reader` 并安装
> 5. 后续可在插件页面进行更新

## 方式二：离线安装插件包
> 1. 下载最新的离线插件包：
>    1. [蓝奏云(密码：nancheung)](https://nancheung.lanzouw.com/b0674v2sj)
>    2. [GitHub Release](https://github.com/nancheung/legado-reader/releases)
> 2. 打开插件市场窗口：`JetBrains IDE` ->`File` -> `Settings` -> `Plugins` -> `Marketplace`
> 3. 打开选择插件包窗口：`⚙齿轮小图标` -> `Install Plugin from Disk...`
> 4. 选择插件包：`选择下载的插件包` -> `OK`

 
# 开始使用
## 阅读
> 1. 界面默认在IDE的右侧Tool Windows下边，名字叫`Legado Reader`
> 2. 在插件首页的文本框中输入阅读APP的web服务地址，点击`刷新`按钮，即可获取到书籍列表。
> 3. 点击书籍列表中的书籍，即可跳转到阅读界面中阅读书籍。

## 设置
> 1. 打开插件设置窗口：`JetBrains IDE` ->`File` -> `Settings` -> `Tools` -> `Legado Reader`
> 2. 可对插件进行默认设置，如：阅读APP的web服务地址、阅读界面的字体大小、阅读界面的字体大小等。

## 使用服务器版阅读([hectorqin/reader](https://github.com/hectorqin/reader)) / 使用`Replit` 免费部署服务版([wy580477/reader-replit](https://github.com/wy580477/reader-replit))
> 1. 获取账号token
>    > 打开服务器版阅读的web页面，登录成功后，在书架页面，开发者工具的控制台中输入：`"accessToken:@"+localStorage.api_token`
> 2. 打开插件设置窗口：`JetBrains IDE` ->`File` -> `Settings` -> `Tools` -> `Legado Reader`
> 3. 在`自定义请求参数`中输入：上一步获取到的值，如：`accessToken:@test:f839648a6de240f839648a6de240`
> 4. 获取当前服务器api地址
>    > 打开服务器版阅读的web页面，登录成功后，在书架页面，开发者工具的控制台中输入：`document.location.protocol+"//"+document.location.host+"/reader3"`
> 5. 在插件首页的地址框中输入api地址，点击`刷新`按钮，即可获取到书籍列表。  

# 已实现功能：
1. 支持在ToolWindow中阅读
1. 支持查看书架目录
1. 阅读章节正文
1. 切换上下章
1. 与阅读APP同步进度
1. 支持自定义字体颜色、大小
1. 支持全局保存配置；
1. 支持服务版阅读;
1. 阅读APP的web服务地址的历史记录;
   ……

# 计划：
1. 在IDE中更多的地方显示正文内容；
1. 支持更多快捷键;

   ……
