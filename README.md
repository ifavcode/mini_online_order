<div align="center">
    <h3>在线扫码点餐小程序 | Mini Online Order</h3>
    <p>
      <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21" />
      <img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot 3.5.9" />
      <img src="https://img.shields.io/badge/Vue-3.5-22FC08D?logo=vuedotjs&logoColor=white" alt="Vue 3.5.22" />
      <img src="https://img.shields.io/badge/Vite-7-646CFF?logo=vite&logoColor=white" alt="Vite 7" />
      <img src="https://img.shields.io/badge/MIT%202.0-D22128?logo=apache&logoColor=white" alt="License" />
    </p>
    <p>
    <p>
        助力每个人拥有自己的下单平台！
    </p>
      <a href="#🚀 快速开始"><strong>🚀 快速开始</strong></a>
      &nbsp;&middot;&nbsp;
      <span>🌐 在线体验</span>
      <a href="https://www.guetzjb.cn/mini-order-front"><strong>前台</strong></a>
      <a href="https://www.guetzjb.cn/mini-order-admin"><strong>后台</strong></a>
      &nbsp;&middot;&nbsp;
      <a href="#✨ 功能特性"><strong>✨ 功能特性</strong></a>
      &nbsp;&middot;&nbsp;
    </p>
</div>


## 📑 目录

- [功能特性](#✨ 功能特性)
- [技术栈](#🛠 技术栈)
- [快速开始](#🚀 快速开始)
  - [本地运行](#本地运行)
  - [部署到服务器](#部署到服务器)
- [说明](#说明)
- [License](#📄 License)

**体验完整内容需运行小程序**

项目已接入微信支付（没有商户号可以在后台关闭支付功能）

## ✨ 功能特性

🎮 **可自定义的商家信息**

支持自定义**店铺名称**、**下单文字说明**、**店铺头像**、**店铺背景图片**、**店铺描述**、**营业时间**、**商家定位**

🔔 **实时订单**

后台在线用户实时接收小程序的订单

**桌号二维码生成**

实时生成、更新桌号二维码

**商家动态**

发布店铺的实时情况~

...其他基础功能

## 🛠 技术栈

| 项目 | 技术 |
|---|---|
| **后端** | Java 21 + Spring Boot 3.5.9 + Spring Data Jpa |
| **后台** | Vue 3.5 + Element Plus + Vite 7 + TypeScript |
| **小程序** | Uniapp + Uni ui + Vite 5 |

**📂 项目结构**

online-ordering-front —— uniapp项目（小程序）**Node.js >= 21**

online-ordering-admin —— vue3项目（管理员后台）**Node.js >= 21**

online-ordering-java —— Spring项目（后台）**Java >= 21**

**⚙ 配置属性**

小程序端`.env`

```properties
VITE_TMPL_ID=4HNjqvQ6ZWAMleu9uIpiZ2EfNwSU_l6iTt4oqAvqSKI # 订单完成模板ID
```

后端`application-dev.yml`

```yaml
wx:
  miniapp:
    configs:
      - appid:  #微信小程序的appid
        secret:  #微信小程序的Secret
        token: #微信小程序消息服务器配置的token
        aesKey: #微信小程序消息服务器配置的EncodingAESKey
        msgDataFormat: JSON
  pay:
    appId: # 小程序appId
    mchId:  # 商户ID
    mchKey: # 商户Key
    keyPath: D:/XXXX/cert/apiclient_cert.p12 # 商户证书
    notifyUrl: http://domain.com/wx/pay/notify # 支付回调
    refund-notify-url: http://domain.com/wx/pay/refund/notify # 退款回调
    tradeType: JSAPI # 类型
```

后端`application.yml`

```yaml
# 用于静态资源存储
server-config:
  ip: # 服务器IP
  port: 22 # SSH端口号
  username: root # 用户名
  password: root # 密码
  dir: /opt/files/ # 存储文件的文件夹
```

## 🚀 快速开始

### 本地运行

确保下面的工具在你的电脑上已经安装

- 微信开发者工具
- VScode、Cursor、其他代码编译器（任选一）
- HBuilder
- IDEA

包管理器推荐使用`pnpm`，没有可使用`npm`直接安装`npm i pnpm -g`

1、online-ordering-front

- `pnpm i`

- 使用HBuilder打开此项目

- 在HBuilder中运行项目（运行 - 运行到小程序模拟器 - 运行微信开发者工具）一般启动完成会自动打开，如果打开失败可以微信开发者工具手动打开online-ordering-front/unpackage/dist/dev

- 配置.env，填写模板ID
  ```javascript
  const tmplIds = ['4HNjqvQ6ZWAMleu9uIpiZ2EfNwSU_l6iTt4oqAvqSKI']
  ```

  `4HNjqvQ6ZWAMleu9uIpiZ2EfNwSU_l6iTt4oqAvqSKI`为示例模板ID，需在[微信公众平台](https://mp.weixin.qq.com/)上申请复制粘贴到此处

- 基础功能 - 订阅消息 - 选用，搜索订单完成通知，仿照下图设置，注意详细内容部分的参数thing6、character_string5、thing1等，每个人得到的参数值不一样，需要在后端处配置，见后端部分
  ![](.\img\订阅消息.png)

2、online-ordering-admin 

- `pnpm i`
- `pnpm dev`
- 访问`http://localhost:8848`

3、online-ordering-java

- 配置src/main/resources/application-dev.yml（application-prod.yml，dev是开发环境，prod是生产环境）
  数据库、rocketmq、wx.miniapp、wx.pay
  wx.miniapp按照提示配置appid、secret（可以在管理 - 开发管理中找到，设置的appid必须和微信开发者工具里的appid一致，否则无法加载）
  商户ID、商户Key前往[微信支付商家平台](https://pay.weixin.qq.com/)查看
  商户证书在微信支付商家平台，账户中心 - API安全 - 商户API证书处申请
  notifyUrl为支付回调，refund-notify-url为退款回调（本地运行时推荐使用natapp内网穿透工具生成一个域名）
  server-config按照要求填写即可
  
- application.yml — spring.profiles.active改为dev
  
- 上面提到的发送订阅消息需要手动调整几个选项，打开src/main/java/cn/guetzjb/onlineorderingjava/service/WxCommonService，找到如下代码
  ```java
  message.setData(Lists.newArrayList(
          new WxMaSubscribeMessage.MsgData("thing6", shopName),
          new WxMaSubscribeMessage.MsgData("character_string5", String.valueOf(orders.getWxOrderNo())),
          new WxMaSubscribeMessage.MsgData("thing1", goodsName),
          new WxMaSubscribeMessage.MsgData("date2", orders.getCreatedAt().format(DEFAULT_FORMATTER)),
          new WxMaSubscribeMessage.MsgData("amount9", orders.getTotalAmount().toString())
  ));
  ```

  按照要求配上即可——thing6对应{{thing6.DATA}}其他同理

### 部署到服务器

1、online-ordering-front

**请改utils/http.js中的BASE_URL为线上地址**

小程序项目直接在微信开发者工具上传代码、再到微信公众平台上扫码登录小程序发布。

2、online-ordering-admin 

`pnpm build`
执行上述命令后生成dist文件夹，将dist文件夹里面所有内容粘贴到服务器nginx的web目录中，nginx相关配置在下面会说。

打开服务器IP即可（注意端口号、目录地址，根据自己nginx配置而定）

3、online-ordering-java

- application.yml — spring.profiles.active改为prod
- 使用命令`mvn package -DskipTests`或者在IDEA中打包右侧maven/Lifecycle/package（需跳过测试，项目有Websocket，测试通不过）
- 将rocketmq（看5）、mysql跑起来
- 将jar包放入服务器，使用`nohup java -jar online-ordering-java-1.0.0.jar >out.txt &`运行
- 使用`tail -f out.txt`查看运行日志

4、nginx

`YOUR_IP`换成你自己的服务器IP

```nginx
# server中
location /assets_other{
    alias /opt/files;
    autoindex on;
    charset utf-8;
}
location /mini_order{
    proxy_pass http://YOUR_IP:9988; #换
    rewrite "^/mini_order/(.*)$" /$1 break;
}
location /mini_order_ws{
    proxy_pass http://YOUR_IP:9988; #换
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
}
```

5、rocketmq

在下载好rocketmq的文件后依次运行（如果运行内存为2G的服务器，默认设置的条件是不够的，需要调整配置，直接在云盘里下载）

注意替换YOUR_IP

```sh
nohup ./bin/mqnamesrv &
tail -f ~/logs/rocketmqlogs/namesrv.log

nohup ./bin/mqbroker -c ./conf/broker.conf -n YOUR_IP:9876 &
tail -f ~/logs/rocketmqlogs/broker.log

nohup ./bin/mqproxy -n YOUR_IP:9876 &
```

执行下面的命令查看是否运行成功

```sh
./bin/mqadmin clusterList -n YOUR_IP:9876
```



## 说明

桌号二维码可在后台生成

生成前需要配置online-ordering-front/.env中的`VITE_QRCODE_URL`，示例为`https://www.guetzjb.cn/assets_other/miniapp/pages/index/index?share=1&path=/pages/index/index&tableCode=`

请根据情况修改







<img src=".\img\1.png" style="zoom:33%;" />

<img src=".\img\2.png" style="zoom:33%;" />

<img src=".\img\3.png" alt="1" style="zoom:33%;" />

<img src=".\img\4.png" style="zoom:33%;" />

<img src=".\img\5.png" style="zoom:33%;" />

<img src=".\img\6.png" style="zoom:50%;" />

<img src=".\img\7.png" style="zoom:50%;" />

<img src=".\img\8.png" style="zoom:50%;" />

## 📄 License

[MIT]([MIT License](https://mit-license.org/)) &copy; 2026 ifavcode

<div align="center">
  <p>
  	如果本项目对你有帮助，欢迎star~
  </p>
  <a href="https://github.com/ifavcode">GitHub</a>
  &nbsp;&middot;&nbsp;
  <span>🌐 在线体验</span>
  <a href="https://www.guetzjb.cn/mini-order-front"><strong>前台</strong></a>
  <a href="https://www.guetzjb.cn/mini-order-front"><strong>后台</strong></a>
</div>
