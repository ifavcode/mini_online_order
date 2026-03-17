<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="用户名称" prop="username">
            <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable style="width: 240px"
              @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="手机号码" prop="phoneNumber">
            <el-input v-model="queryParams.phoneNumber" placeholder="请输入手机号码" clearable style="width: 240px"
              @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 240px">
              <el-option key="1" label="启用" value="1" />
              <el-option key="0" label="禁用" value="0" />
            </el-select>
          </el-form-item>
          <!-- <el-form-item label="创建时间" style="width: 308px">
            <el-date-picker v-model="dateRange" value-format="YYYY-MM-DD" type="daterange" range-separator="-"
              start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
          </el-form-item> -->
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain :icon="Plus" @click="handleAdd" v-perms="['system:user:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain :icon="Edit" :disabled="single" @click="handleUpdate"
              v-perms="['system:user:add']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain :icon="Delete" :disabled="multiple" @click="handleDelete"
              v-perms="['system:user:delete']">删除</el-button>
          </el-col>
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column label="用户编号" align="center" key="userId" prop="userId" v-if="columns[0].visible" />
          <el-table-column label="用户名称" align="center" key="username" prop="username" v-if="columns[1].visible"
            :show-overflow-tooltip="true" />
          <el-table-column label="用户昵称" align="center" key="nickname" prop="nickname" v-if="columns[2].visible"
            :show-overflow-tooltip="true" />
          <el-table-column label="手机号码" align="center" key="phoneNumber" prop="phoneNumber" v-if="columns[4].visible" />
          <el-table-column label="权限字符" align="center" key="permission" prop="permission" v-if="columns[6].visible"
            width="120">
            <template #default="scope">
              <p v-if="scope.row.permission">
                <el-tag v-for="item in scope.row.permission.split(',')" size="small">{{
                  item
                }}</el-tag>
              </p>
            </template>
          </el-table-column>
          <el-table-column label="openid" align="center" key="openid" prop="openid">
            <template #default="scope">
              <p>
                {{ scope.row.openid }}
              </p>
            </template>
          </el-table-column>
          <el-table-column label="状态" align="center" key="status" v-if="columns[5].visible">
            <template #default="scope">
              <el-switch v-model="scope.row.status" active-value="1" inactive-value="0"
                @change="handleStatusChange(scope.row)"></el-switch>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" align="center" prop="createAt" v-if="columns[6].visible" width="160">
            <template #default="scope">
              <span>{{ scope.row.createAt }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
            <template #default="scope">
              <el-tooltip content="修改" placement="top">
                <el-button link type="primary" :icon="Edit" @click="handleUpdate(scope.row)"
                  v-perms="['system:user:add']"></el-button>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-button link type="primary" :icon="Delete" @click="handleDelete(scope.row)"
                  v-perms="['system:user:remove']"></el-button>
              </el-tooltip>
              <el-tooltip content="重置密码" placement="top">
                <el-button link type="primary" :icon="Key" @click="handleResetPwd(scope.row)"
                  v-perms="['system:user:resetPwd']"></el-button>
              </el-tooltip>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination class="mt-4" v-show="total > 0" :current-page="queryParams.pageNum"
          :page-size="queryParams.pageSize" :total="total" :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="(page) => { queryParams.pageNum = page; getList(); }"
          @size-change="(size) => { queryParams.pageSize = size; queryParams.pageNum = 1; getList(); }" />
      </el-col>
    </el-row>

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form :model="form" :rules="rules" ref="userRef" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入用户昵称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户性别">
              <el-select v-model="form.sex" placeholder="请选择">
                <el-option label="男" value="1"></el-option>
                <el-option label="女" value="0"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号码" prop="phoneNumber">
              <el-input v-model="form.phoneNumber" placeholder="请输入手机号码" maxlength="11" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户名称" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名称" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入用户密码" type="password" minlength="5" maxlength="20"
                show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="权限字符">
              <el-input v-model="form.permission" placeholder="多个可用逗号分隔(英文)" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio key="1" value="1">启用</el-radio>
                <el-radio key="0" value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入内容"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts" name="User">
import {
  Search,
  Plus,
  Edit,
  Delete,
  Refresh,
  Key
} from "@element-plus/icons-vue";
import {
  changeUserStatus,
  listUser,
  resetUserPwd,
  delUser,
  getUser,
  updateUser,
  addUser,
} from "@/api/user";
import { useRouter } from "vue-router";
import { ref, reactive, toRefs, nextTick } from "vue";
import { ElMessageBox, ElMessage } from "element-plus";
import type { FormInstance, FormRules } from "element-plus";

const userList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);
const postOptions = ref([]);
const roleOptions = ref([]);
const userRef = ref<FormInstance>();
const queryRef = ref<FormInstance>();
// 列显隐信息
const columns = ref([
  { key: 0, label: `用户编号`, visible: true },
  { key: 1, label: `用户名称`, visible: true },
  { key: 2, label: `用户昵称`, visible: true },
  { key: 3, label: `角色名称`, visible: true },
  { key: 4, label: `手机号码`, visible: true },
  { key: 5, label: `状态`, visible: true },
  { key: 6, label: `创建时间`, visible: true },
  { key: 7, label: `权限字符`, visible: true },
]);

interface UserForm {
  userId?: number;
  deptId?: number;
  username?: string;
  nickname?: string;
  password?: string;
  phoneNumber?: string;
  email?: string;
  sex?: string;
  status?: string;
  remark?: string;
  postIds?: Array<number | string>;
  roleId?: number;
  roleIds?: Array<number | string>;
  baseCode?: string | string[];
  permission?: string;
}
interface QueryParams {
  pageNum: number;
  pageSize: number;
  username?: string;
  phoneNumber?: string;
  status?: string;
  deptId?: number;
}

const data = reactive<{
  form: UserForm;
  queryParams: QueryParams;
  rules: FormRules;
}>({
  form: {
    userId: undefined,
    deptId: undefined,
    username: undefined,
    nickname: undefined,
    password: undefined,
    phoneNumber: undefined,
    email: undefined,
    sex: undefined,
    status: "1",
    remark: undefined,
    postIds: [],
    roleId: undefined,
    roleIds: [],
    baseCode: undefined,
    permission: undefined
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    username: undefined,
    phoneNumber: undefined,
    status: undefined,
  },
  rules: {
    username: [
      { required: true, message: "用户名称不能为空", trigger: "blur" },
      {
        min: 2,
        max: 20,
        message: "用户名称长度必须介于 2 和 20 之间",
        trigger: "blur"
      }
    ],
    nickname: [{ required: true, message: "用户昵称不能为空", trigger: "blur" }],
    password: [
      { required: true, message: "用户密码不能为空", trigger: "blur" },
      {
        min: 5,
        max: 20,
        message: "用户密码长度必须介于 5 和 20 之间",
        trigger: "blur"
      },
      {
        pattern: /^[^<>"'|\\]+$/,
        message: "不能包含非法字符：< > \" ' \\\ |",
        trigger: "blur"
      }
    ],
    email: [
      {
        type: "email",
        message: "请输入正确的邮箱地址",
        trigger: ["blur", "change"]
      }
    ],
    phoneNumber: [
      {
        pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
        message: "请输入正确的手机号码",
        trigger: "blur"
      }
    ],
    baseCode: [{ required: false, message: "基地不能为空", trigger: "blur" }],
    department: [{ required: false, message: "车间不能为空", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询用户列表 */
function getList() {
  loading.value = true;
  listUser(queryParams.value).then(res => {
    loading.value = false;
    userList.value = res.content;
    total.value = res.totalElements;
  });
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  queryParams.value.username = undefined
  queryParams.value.phoneNumber = undefined
  queryParams.value.status = undefined
  handleQuery();
}

/** 删除按钮操作 */
function handleDelete(row) {
  const userIds = row.userId || ids.value;
  ElMessageBox.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？', "提示", {
    type: "warning"
  })
    .then(() => delUser(userIds))
    .then(() => {
      getList();
      ElMessage.success("删除成功");
    })
    .catch((e) => {
      console.log(e);
    });
}

/** 用户状态修改  */
function handleStatusChange(row) {
  console.log(row.status);

  let text = row.status === "1" ? "启用" : "停用";
  ElMessageBox.confirm('确认要"' + text + '""' + row.username + '"用户吗?', "提示", {
    type: "warning"
  })
    .then(() => changeUserStatus({ userId: row.userId, status: row.status }))
    .then(() => {
      ElMessage.success(text + "成功");
    })
    .catch(() => {
      // 撤销开关显示，保持与后端一致
      row.status = row.status === "1" ? "0" : "1";
    });
}

/** 重置密码按钮操作 */
function handleResetPwd(row) {
  ElMessageBox.prompt('请输入"' + row.username + '"的新密码', "提示", {
    confirmButtonText: "确定",
    cancelButtonText: "取消",
    closeOnClickModal: false,
    inputPattern: /^.{5,20}$/,
    inputErrorMessage: "用户长度必须介于 5 和 20 之间",
    inputValidator: value => {
      if (/<|>|"|'|\||\\/.test(value)) {
        return "不能包含非法字符：< > \" ' \\\ |";
      }
      return true;
    }
  })
    .then(({ value }) =>
      resetUserPwd({ userId: row.userId, password: value }).then(() => {
        ElMessage.success("修改成功，新密码是：" + value);
      })
    )
    .catch(() => { });
}

/** 选择条数  */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.userId);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}
/** 重置操作表单 */
function reset() {
  form.value = {
    userId: undefined,
    deptId: undefined,
    username: undefined,
    nickname: undefined,
    password: undefined,
    phoneNumber: undefined,
    email: undefined,
    sex: undefined,
    status: "0",
    remark: undefined,
    postIds: [],
    roleId: undefined,
    permission: undefined
  };
  userRef.value?.resetFields();
  userRef.value?.clearValidate();
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  open.value = true;
  title.value = "添加用户";
  form.value.status = '1'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const userId = row.userId || ids.value;
  getUser(userId).then(response => {
    form.value = response;
    postOptions.value = response.posts;
    roleOptions.value = response.roles;
    form.value.postIds = response.postIds;
    form.value.roleId = response.roleIds ? response.roleIds[0] : null;
    open.value = true;
    title.value = "修改用户";
    form.value.password = "";
  });
}

/** 提交按钮 */
function submitForm() {
  userRef.value?.validate(valid => {
    if (!valid) return;
    const tmpForm = {
      ...form.value,
    };
    const request = form.value.userId != undefined ? updateUser(tmpForm) : addUser(tmpForm);
    request.then(() => {
      ElMessage.success(form.value.userId != undefined ? "修改成功" : "新增成功");
      open.value = false;
      getList();
    });
  });
}

getList();
</script>
