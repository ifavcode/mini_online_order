/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Host           : localhost:3306
 Source Schema         : mini_ordering

 Target Server Type    : MySQL
 File Encoding         : 65001

 Date: 04/03/2026 12:57:32
*/

SET NAMES utf8;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for address
-- ----------------------------
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `create_at` datetime(6) NOT NULL,
  `detail_address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `receiver` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKrbi6ij3u9oy8qrwyb2r0q3v09`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKrbi6ij3u9oy8qrwyb2r0q3v09` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of address
-- ----------------------------

-- ----------------------------
-- Table structure for business_dynamic
-- ----------------------------
DROP TABLE IF EXISTS `business_dynamic`;
CREATE TABLE `business_dynamic`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `images` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK1a7ls4av4yybk4s1f87ob29ps`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK1a7ls4av4yybk4s1f87ob29ps` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of business_dynamic
-- ----------------------------
INSERT INTO `business_dynamic` VALUES (1, '2026-01-22 17:53:13.266692', '新店开业🎇', 'https://www.guetzjb.cn/assets_other/2026-01-23/ha_fu_bi.png,https://www.guetzjb.cn/assets_other/2026-01-23/chao_xi_jian_yu_di_tu_4.png,https://www.guetzjb.cn/assets_other/2026-01-23/sheng_wu_yang_ben_shi.png,https://www.guetzjb.cn/assets_other/2026-01-23/shi_yan_shu_ju.png', NULL);
INSERT INTO `business_dynamic` VALUES (3, '2026-01-23 11:26:12.175594', '欢迎各位点餐', 'https://www.guetzjb.cn/assets_other/2026-01-23/07abb7b79c0804acf3089dbc3dcb853e.jpg', NULL);

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `base_price` decimal(10, 2) NOT NULL,
  `category_id` int NOT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `has_spec` int NULL DEFAULT NULL,
  `img_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sort_order` int NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `stock` int NULL DEFAULT -1 COMMENT '库存: -1无限',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_goods_menu_category`(`category_id` ASC) USING BTREE,
  CONSTRAINT `fk_goods_menu_category` FOREIGN KEY (`category_id`) REFERENCES `menu_category` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 106 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (104, 0.10, 1, '2026-03-04 10:03:57.123325', '两个版本，一个带后台，一个不带后台', 1, 'https://www.guetzjb.cn/assets_other/2026-03-04/banner1.feb6ce29.jpg', '企业官网网站模板', 1, 1, '2026-03-04 12:47:42.387789', -1);
INSERT INTO `goods` VALUES (105, 0.20, 2, '2026-03-04 10:06:12.030814', '开箱即用！', 0, 'https://www.guetzjb.cn/assets_other/2026-03-04/40cd359bd60659a066ff544bc0d87f4e.png', '微信小程序点餐源码模板', 1, 1, '2026-03-04 12:47:50.654518', -1);

-- ----------------------------
-- Table structure for menu_category
-- ----------------------------
DROP TABLE IF EXISTS `menu_category`;
CREATE TABLE `menu_category`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sort_order` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menu_category
-- ----------------------------
INSERT INTO `menu_category` VALUES (1, '2026-01-08 11:34:13.000000', '🏆 推荐', 1);
INSERT INTO `menu_category` VALUES (2, '2026-01-12 16:20:49.538171', '😊热销', 2);

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `goods_id` int NOT NULL,
  `order_id` int NOT NULL,
  `subtotal` decimal(10, 2) NOT NULL,
  `unit_price` decimal(10, 2) NOT NULL,
  `spec_group_json` text NULL COMMENT '规格组JSON',
  `count` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order`(`order_id` ASC) USING BTREE,
  INDEX `FKati1k6nilkh8m762nn3736kea`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `FKati1k6nilkh8m762nn3736kea` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKt4dc2r9nbvbujrljv3e23iibt` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 234 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (230, 105, 138, 0.20, 0.20, '[]', 1);
INSERT INTO `order_item` VALUES (231, 104, 139, 0.10, 0.10, '[{\"id\": 20, \"name\": \"配置\", \"goods\": {\"id\": 104, \"name\": \"企业官网网站模板\", \"stock\": -1, \"imgUrl\": \"https://www.guetzjb.cn/assets_other/2026-03-04/banner1.feb6ce29.jpg\", \"status\": 1, \"hasSpec\": 1, \"category\": {\"id\": 1, \"name\": \"🏆 推荐\", \"createdAt\": \"2026-01-08 11:34:13\", \"sortOrder\": 1}, \"basePrice\": 0.1, \"createdAt\": \"2026-03-04 10:03:57\", \"sortOrder\": 1, \"updatedAt\": \"2026-03-04T12:47:42.387789\", \"categoryId\": 1, \"description\": \"两个版本，一个带后台，一个不带后台\"}, \"goodsId\": 104, \"sortOrder\": 1, \"specItems\": [{\"id\": 28, \"name\": \"不带后台\", \"stock\": -1, \"status\": 1, \"sortOrder\": 1, \"extraPrice\": 0}], \"selectType\": \"one\"}]', 1);
INSERT INTO `order_item` VALUES (232, 104, 140, 0.10, 0.10, '[{\"id\": 20, \"name\": \"配置\", \"goods\": {\"id\": 104, \"name\": \"企业官网网站模板\", \"stock\": -1, \"imgUrl\": \"https://www.guetzjb.cn/assets_other/2026-03-04/banner1.feb6ce29.jpg\", \"status\": 1, \"hasSpec\": 1, \"category\": {\"id\": 1, \"name\": \"🏆 推荐\", \"createdAt\": \"2026-01-08 11:34:13\", \"sortOrder\": 1}, \"basePrice\": 0.1, \"createdAt\": \"2026-03-04 10:03:57\", \"sortOrder\": 1, \"updatedAt\": \"2026-03-04T12:47:42.387789\", \"categoryId\": 1, \"description\": \"两个版本，一个带后台，一个不带后台\"}, \"goodsId\": 104, \"sortOrder\": 1, \"specItems\": [{\"id\": 28, \"name\": \"不带后台\", \"stock\": -1, \"status\": 1, \"sortOrder\": 1, \"extraPrice\": 0}], \"selectType\": \"one\"}]', 1);
INSERT INTO `order_item` VALUES (233, 104, 141, 0.10, 0.10, '[{\"id\": 20, \"name\": \"配置\", \"goods\": {\"id\": 104, \"name\": \"企业官网网站模板\", \"stock\": -1, \"imgUrl\": \"https://www.guetzjb.cn/assets_other/2026-03-04/banner1.feb6ce29.jpg\", \"status\": 1, \"hasSpec\": 1, \"category\": {\"id\": 1, \"name\": \"🏆 推荐\", \"createdAt\": \"2026-01-08 11:34:13\", \"sortOrder\": 1}, \"basePrice\": 0.1, \"createdAt\": \"2026-03-04 10:03:57\", \"sortOrder\": 1, \"updatedAt\": \"2026-03-04T12:47:42.387789\", \"categoryId\": 1, \"description\": \"两个版本，一个带后台，一个不带后台\"}, \"goodsId\": 104, \"sortOrder\": 1, \"specItems\": [{\"id\": 28, \"name\": \"不带后台\", \"stock\": -1, \"status\": 1, \"sortOrder\": 1, \"extraPrice\": 0}], \"selectType\": \"one\"}]', 2);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `order_no` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `status` tinyint NULL DEFAULT 1 COMMENT '1待支付 2已支付 3已完成 4已取消',
  `total_amount` decimal(10, 2) NOT NULL,
  `table_id` int NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `pickup_time` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `user_id` int NULL DEFAULT NULL,
  `wx_order_no` varchar(52) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `pay_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK2x8p3n1c0ubla95xwx0dt5pak`(`table_id` ASC) USING BTREE,
  INDEX `FKa6ffsu1w47sjotgectd8wkcw6`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK2x8p3n1c0ubla95xwx0dt5pak` FOREIGN KEY (`table_id`) REFERENCES `table_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKa6ffsu1w47sjotgectd8wkcw6` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 142 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (138, '2026-03-04 12:48:13.981350', '0001', 2, 0.20, NULL, '', 'dinein', '立刻制作', '13136592050', 5, '8a043b4dfcb849b29782ed2ac90b4cb5', NULL);
INSERT INTO `orders` VALUES (139, '2026-03-04 12:49:01.231247', '0002', 2, 0.10, NULL, '', 'dinein', '立刻制作', '13136592050', 5, '76474b27d8214aee876292d14e9e133a', NULL);
INSERT INTO `orders` VALUES (140, '2026-03-04 12:55:45.996905', '0003', 2, 0.10, NULL, '', 'dinein', '立刻制作', '13136592050', 5, '514aab48fcb34b6ca13cf73a244b581a', NULL);
INSERT INTO `orders` VALUES (141, '2026-03-04 12:56:07.776529', '0004', 1, 0.20, NULL, '', 'dinein', '立刻制作', '13136592050', 5, '7d60f76f35534c6fb60039bc94a93750', NULL);

-- ----------------------------
-- Table structure for refund_record
-- ----------------------------
DROP TABLE IF EXISTS `refund_record`;
CREATE TABLE `refund_record`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `refund_account` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `refund_fee` int NULL DEFAULT NULL,
  `refund_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `refund_recv_accout` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `refund_request_source` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `refund_status` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `success_time` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `total_fee` int NULL DEFAULT NULL,
  `wx_order_no` varchar(52) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of refund_record
-- ----------------------------
INSERT INTO `refund_record` VALUES (1, 'REFUND_SOURCE_RECHARGE_FUNDS', 1, '50302206382026022516747856342', '支付用户零钱', 'API', 'SUCCESS', '2026-02-25 15:04:14', 1, '8882aa195e644e189bb1cedd2d60a474');
INSERT INTO `refund_record` VALUES (2, 'REFUND_SOURCE_RECHARGE_FUNDS', 1, '50302306342026022579891987103', '支付用户零钱', 'API', 'SUCCESS', '2026-02-25 15:06:26', 1, '77bf692f93234a57bab5bb3968e07bbb');
INSERT INTO `refund_record` VALUES (3, 'REFUND_SOURCE_RECHARGE_FUNDS', 1, '50303206272026022571082861366', '支付用户零钱', 'API', 'SUCCESS', '2026-02-25 15:08:37', 1, 'ba1b9edb7e694bde85505bef81ecbf74');
INSERT INTO `refund_record` VALUES (4, 'REFUND_SOURCE_RECHARGE_FUNDS', 1, '50300906082026022517947576404', '支付用户零钱', 'API', 'SUCCESS', '2026-02-25 15:16:15', 1, 'e36acc18c3f44e7c9f4eb4c1220b4814');

-- ----------------------------
-- Table structure for spec_group
-- ----------------------------
DROP TABLE IF EXISTS `spec_group`;
CREATE TABLE `spec_group`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `goods_id` int NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `select_type` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sort_order` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKfcv514qgnxm9mx2drsytd739`(`goods_id` ASC) USING BTREE,
  CONSTRAINT `FKfcv514qgnxm9mx2drsytd739` FOREIGN KEY (`goods_id`) REFERENCES `goods` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of spec_group
-- ----------------------------
INSERT INTO `spec_group` VALUES (20, 104, '配置', 'one', 1);

-- ----------------------------
-- Table structure for spec_item
-- ----------------------------
DROP TABLE IF EXISTS `spec_item`;
CREATE TABLE `spec_item`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `extra_price` decimal(10, 2) NULL DEFAULT NULL,
  `group_id` int NOT NULL,
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `sort_order` int NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 1,
  `stock` int NULL DEFAULT -1 COMMENT '库存: -1无限',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKc545b94bbfvifj7s2g74cjpx6`(`group_id` ASC) USING BTREE,
  CONSTRAINT `FKc545b94bbfvifj7s2g74cjpx6` FOREIGN KEY (`group_id`) REFERENCES `spec_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of spec_item
-- ----------------------------
INSERT INTO `spec_item` VALUES (28, 0.00, 20, '不带后台', 1, 1, -1);
INSERT INTO `spec_item` VALUES (29, 50.00, 20, '带后台', 2, 1, -1);

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `dict_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `dict_value` varchar(1024) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKrkramx8ani1wio1gfjwajbdn5`(`dict_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (1, NULL, 'shop_name', '软件模板专卖');
INSERT INTO `sys_dict` VALUES (2, NULL, 'shop_desc', '源码专卖哦');
INSERT INTO `sys_dict` VALUES (3, NULL, 'business_hours', '{\"weekDays\":[1,2,3,4,5],\"startTime\":\"08:00\",\"endTime\":\"20:00\"}');
INSERT INTO `sys_dict` VALUES (4, NULL, 'shop_location', '浙江省杭州市上城区彭埠街道中豪五星国际');
INSERT INTO `sys_dict` VALUES (5, NULL, 'choose_lng_lat', '{\"lat\":30.290408,\"lng\":120.22329}');
INSERT INTO `sys_dict` VALUES (6, NULL, 'shop_image', 'https://www.guetzjb.cn/assets_other/2026-01-16/lologo.jpg');
INSERT INTO `sys_dict` VALUES (7, NULL, 'shop_phone', '13136592050');
INSERT INTO `sys_dict` VALUES (8, NULL, 'shop_bg_image', 'https://www.guetzjb.cn/assets_other/2026-03-04/R-C.jpg');
INSERT INTO `sys_dict` VALUES (9, NULL, 'pay_switch', '1');
INSERT INTO `sys_dict` VALUES (11, NULL, 'order_tip', '1');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `component` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `front` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '1前台菜单 0不是前台菜单',
  `hidden` bit(1) NULL DEFAULT NULL,
  `icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `menu_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `order_num` int NULL DEFAULT NULL COMMENT '排序，越小越靠前',
  `parent_id` bigint NULL DEFAULT NULL,
  `route_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路由名称',
  `route_path` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'system/index', '2025-09-26 11:30:16.000000', '0', b'0', 'material-symbols:settings', '系统管理', 1, 0, '系统管理', '/system');
INSERT INTO `sys_menu` VALUES (2, 'system/menu/index', '2025-09-26 11:30:56.000000', '0', b'0', 'material-symbols:menu', '菜单管理', 1, 1, '菜单管理', '/system/menu');
INSERT INTO `sys_menu` VALUES (3, 'goods/index', '2026-01-12 11:34:46.990000', NULL, b'0', 'material-symbols:shopping-cart-rounded', '商品管理', 2, 4, '商品管理', '/goods/index');
INSERT INTO `sys_menu` VALUES (4, NULL, '2026-01-12 14:09:52.792000', NULL, b'0', 'material-symbols:shopping-cart-rounded', '商品类', 3, 0, '商品类', '/goods');
INSERT INTO `sys_menu` VALUES (5, 'goods/category', '2026-01-12 15:17:31.708000', NULL, b'0', 'material-symbols:category-rounded', '类别管理', 2, 4, '类别管理', '/goods/category');
INSERT INTO `sys_menu` VALUES (6, 'merchant_info/index', '2026-01-13 11:21:47.318000', NULL, b'0', 'material-symbols:chat-info-rounded', '商家信息', 2, 0, '商家信息', '/merchant_info/index');
INSERT INTO `sys_menu` VALUES (7, 'table/index', '2026-01-13 16:08:02.803000', NULL, b'0', 'material-symbols:table-restaurant', '桌号管理', 4, 0, '桌号管理', '/table/index');
INSERT INTO `sys_menu` VALUES (8, 'order/index', '2026-01-13 16:11:01.880000', NULL, b'0', 'material-symbols:date-range', '订单管理', 5, 0, '订单管理', '/order/index');
INSERT INTO `sys_menu` VALUES (9, 'system/user/index', '2026-01-20 09:32:15.401000', NULL, b'0', 'ep:user-filled', '用户管理', 2, 1, '用户管理', '/system/user');
INSERT INTO `sys_menu` VALUES (10, 'business_dynamic/index', '2026-01-22 17:34:00.000000', NULL, b'0', 'material-symbols:assignment-ind', '商家动态', 6, 0, '商家动态', '/business_dynamic/index');
INSERT INTO `sys_menu` VALUES (11, NULL, '2026-02-12 14:59:05.774000', NULL, b'0', 'cib:wechat', '微信支付', 7, 0, '微信支付', '/wechat');
INSERT INTO `sys_menu` VALUES (12, 'wechat/query', '2026-02-12 15:00:12.738000', NULL, b'0', 'cib:wechat', '商户订单号查询订单', 1, 11, '商户订单号查询订单', '/wechat/query');
INSERT INTO `sys_menu` VALUES (13, 'wechat/refund', '2026-02-25 15:22:27.662000', NULL, b'0', 'cib:wechat', '退款记录', 2, 11, '退款记录', '/wechat/refund');
INSERT INTO `sys_menu` VALUES (14, 'update/index', '2026-02-27 14:19:05.108000', NULL, b'0', 'material-symbols:event-note', '更新日志', 8, 0, '更新日志', '/update/index');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `create_at` datetime(6) NOT NULL,
  `del_flag` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `nickname` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `permission` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `phone_number` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `sex` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `status` char(1) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `openid` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, NULL, '2026-02-25 15:13:41.000000', '0', NULL, '服务器', NULL, NULL, NULL, NULL, '1', NULL, NULL);
INSERT INTO `sys_user` VALUES (2, NULL, '2026-01-15 13:56:04.042515', '0', NULL, '未命名用户', NULL, '', NULL, NULL, '1', NULL, 'otZBr5MeUH4O6zySFU6kM9qSRxLc');
INSERT INTO `sys_user` VALUES (3, 'https://avatars.githubusercontent.com/u/83257074', '2025-12-05 15:59:02.000000', '0', '1308003218@qq.com', 'biu', '$2a$10$s5vDkytJjx5rO.Zpm1stKe/QOUuEqC/tr8DiecJ2jdaC53TsZdH36', '*', '13333333333', '1', '1', 'admin', NULL);
INSERT INTO `sys_user` VALUES (4, NULL, '2026-01-19 21:43:36.511509', '0', NULL, '未命名用户', NULL, '', NULL, NULL, '1', NULL, 'otZBr5JO5KgNrVkd81PV68YX9DF0');
INSERT INTO `sys_user` VALUES (5, 'https://wx.qlogo.cn/mmhead/zsUXYY6y4cKMtXcxs4PFicvriayD87E3XuX9RKtoibICHkP3IOzS910Uic86tEbL8jk5lZanJcJBZrs/0', '2026-02-11 16:50:53.041132', '0', NULL, 'emme', NULL, '', '13136592050', NULL, '1', NULL, 'oy-5B3X_4gZIo1IaY9EBewqDGCJ4');

-- ----------------------------
-- Table structure for table_info
-- ----------------------------
DROP TABLE IF EXISTS `table_info`;
CREATE TABLE `table_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `qr_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `table_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `table_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of table_info
-- ----------------------------
INSERT INTO `table_info` VALUES (1, '2026-01-13 16:41:03.000000', 'https://www.guetzjb.cn/assets_other/2026-02-25/a2c27e5772ed431694e3c39709216572.png', 'X01', '1号桌');
INSERT INTO `table_info` VALUES (2, '2026-01-13 16:43:52.428810', 'https://www.guetzjb.cn/assets_other/2026-02-25/75b9359d4406403d9ac738e12bbf939f.png', 'X02', '2号桌');

SET FOREIGN_KEY_CHECKS = 1;
