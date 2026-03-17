interface MenuCategory {
  id?: number

  name?: string

  sortOrder?: number

  createdAt?: any

  goodsList?: Goods[]
}

export interface MenuCategory {
  id?: number

  name?: string

  sortOrder?: number

  createdAt?: any

  goodsList?: any
}

export interface SpecGroup {
  id?: number

  goodsId?: number

  name?: string

  selectType?: any

  sortOrder?: number

  goods?: Goods

  specItems?: SpecItem[]
}

export interface SpecItem {
  id?: number

  name?: string

  extraPrice?: number

  stock?: number

  status?: number

  sortOrder?: number

  specGroup?: SpecGroup
}

export interface Table {
  id?: number

  tableName?: string

  tableCode?: string

  qrCode?: string

  createdAt?: any

  orders?: any
}


export interface SysUser {
  userId?: number

  username?: string

  password?: string

  nickname?: string

  email?: string

  phoneNumber?: string

  sex?: any

  openid?: string

  avatar?: string

  permission?: string

  status?: any

  delFlag?: any

  createAt?: any

  addressList?: any

  ordersList?: any
}

export interface Orders {
  id?: number

  orderNo?: string

  totalAmount?: number

  orderType?: string

  pickupTime?: string

  phone?: string

  remark?: string

  status?: number

  createdAt?: any

  orderItems?: any

  table?: Table

  sysUser?: SysUser
}



export interface Goods {
  id?: number

  categoryId?: number

  name?: string

  description?: string

  stock?: number

  basePrice?: number

  imgUrl?: string

  hasSpec?: number

  status?: number

  sortOrder?: number

  createdAt?: any

  updatedAt?: any

  category?: MenuCategory

  specGroups?: SpecGroup[]

  orderItem?: Orders
}
