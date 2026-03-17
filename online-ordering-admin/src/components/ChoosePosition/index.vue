<script setup lang="ts">
import { ElMessage } from "element-plus";
import { ref, nextTick, reactive, onMounted } from "vue";
import AMapLoader from "@amap/amap-jsapi-loader";
import { Location } from "@element-plus/icons-vue";

const provinceOptions = [
  {
    label: "全国",
    value: "全国"
  },
  // 23个省
  {
    label: "河北省",
    value: "河北省"
  },
  {
    label: "山西省",
    value: "山西省"
  },
  {
    label: "辽宁省",
    value: "辽宁省"
  },
  {
    label: "吉林省",
    value: "吉林省"
  },
  {
    label: "黑龙江省",
    value: "黑龙江省"
  },
  {
    label: "江苏省",
    value: "江苏省"
  },
  {
    label: "浙江省",
    value: "浙江省"
  },
  {
    label: "安徽省",
    value: "安徽省"
  },
  {
    label: "福建省",
    value: "福建省"
  },
  {
    label: "江西省",
    value: "江西省"
  },
  {
    label: "山东省",
    value: "山东省"
  },
  {
    label: "河南省",
    value: "河南省"
  },
  {
    label: "湖北省",
    value: "湖北省"
  },
  {
    label: "湖南省",
    value: "湖南省"
  },
  {
    label: "广东省",
    value: "广东省"
  },
  {
    label: "海南省",
    value: "海南省"
  },
  {
    label: "四川省",
    value: "四川省"
  },
  {
    label: "贵州省",
    value: "贵州省"
  },
  {
    label: "云南省",
    value: "云南省"
  },
  {
    label: "陕西省",
    value: "陕西省"
  },
  {
    label: "甘肃省",
    value: "甘肃省"
  },
  {
    label: "青海省",
    value: "青海省"
  },
  {
    label: "台湾省",
    value: "台湾省"
  },
  // 5个自治区
  {
    label: "内蒙古自治区",
    value: "内蒙古自治区"
  },
  {
    label: "广西壮族自治区",
    value: "广西壮族自治区"
  },
  {
    label: "西藏自治区",
    value: "西藏自治区"
  },
  {
    label: "宁夏回族自治区",
    value: "宁夏回族自治区"
  },
  {
    label: "新疆维吾尔自治区",
    value: "新疆维吾尔自治区"
  },
  // 4个直辖市
  {
    label: "北京市",
    value: "北京市"
  },
  {
    label: "上海市",
    value: "上海市"
  },
  {
    label: "天津市",
    value: "天津市"
  },
  {
    label: "重庆市",
    value: "重庆市"
  },
  // 2个特别行政区
  {
    label: "香港特别行政区",
    value: "香港特别行政区"
  },
  {
    label: "澳门特别行政区",
    value: "澳门特别行政区"
  }
];

let map: any;
const open = ref(false);
let marker: any;
const emit = defineEmits(["chooseEnd"]);
let AMap: any;

const apiKey = import.meta.env.VITE_GAODE_MAP_KEY;
const apiCode = import.meta.env.VITE_GAODE_MAP_CODE;

const props = withDefaults(
  defineProps<{
    width?: string;
    height?: string;
    lng?: number;
    lat?: number;
  }>(),
  {
    width: "80vw",
    height: "70vh"
  }
);
const mapState = reactive({
  city: "全国",
  searchValue: ""
});

function initMap() {
  nextTick(() => {
    let center: [number, number];
    if (props.lng && props.lat) {
      center = [props.lng, props.lat];
    } else {
      center = [120.075894, 30.308373];
    }
    AMapLoader.load({
      key: apiKey,
      version: "2.0", // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
      plugins: [
        "AMap.Scale",
        "AMap.Geocoder",
        "AMap.AutoComplete",
        "AMap.PlaceSearch"
      ] //需要使用的的插件列表，如比例尺'AMap.Scale'，支持添加多个如：['...','...']
    })
      .then(v => {
        AMap = v;
        map = new AMap.Map("chooseMap", {
          viewMode: "3D",
          zoom: 11,
          center
        });
        map.setDefaultCursor("default");
        if (props.lng && props.lat) {
          handleMapClick({
            lnglat: {
              lng: props.lng,
              lat: props.lat
            }
          });
        }
        map.on("click", handleMapClick);
      })
      .catch(e => {
        console.log(e);
      });
  });
}

let chooseLngLat = ref(null);
function handleMapClick(ev: any) {
  let lnglat = ev.lnglat;
  if (marker) {
    map.remove(marker);
  }
  marker = new AMap.Marker({
    position: new AMap.LngLat(lnglat.lng, lnglat.lat),
    title: "标记点"
  });
  map.add(marker);
  chooseLngLat.value = lnglat;
  setDetails(lnglat);
}

function choose() {
  open.value = true;
  initMap();
}

function dialogClose() {}

let queryDetails;

function setDetails(lnglat: [number, number]) {
  let geocoder = new AMap.Geocoder({
    city: mapState.city
  });

  geocoder.getAddress(lnglat, function (status, result) {
    if (status === "complete" && result.info === "OK") {
      // result为对应的地理位置详细信息
      queryDetails = result.regeocode.formattedAddress;
      mapState.searchValue = queryDetails;
    }
  });
}

function handleSelect(item: any) {
  setDetails([item.location.lng, item.location.lat]);
  if (item.location.lng) {
    handleMapClick({
      lnglat: {
        lng: item.location.lng,
        lat: item.location.lat
      }
    });
    mapState.searchValue = item.name;
    map.setZoomAndCenter(18, [item.location.lng, item.location.lat]);
  } else {
    //@ts-ignore
    ElMessage.error("选项不是一个具体的位置");
  }
}

const querySearch = (queryString, cb) => {
  let autoOptions = {
    city: mapState.city || "全国"
  };
  //@ts-ignore
  let autoComplete = new AMap.AutoComplete(autoOptions);
  autoComplete.search(queryString, function (status, result) {
    if (!queryString) return cb([]);
    console.log(status, result);

    const transArr = result.tips;
    cb(transArr);
  });
};

function confirm() {
  open.value = false;
  emit("chooseEnd", {
    chooseLngLat: chooseLngLat.value,
    queryDetails
  });
}

onMounted(() => {
  // @ts-ignore
  window._AMapSecurityConfig = {
    securityJsCode: apiCode
  };
});
</script>

<template>
  <div>
    <el-button @click="choose">
      <el-icon><Location /></el-icon>
      <span>地图选点</span>
    </el-button>
    <el-dialog
      title="选择位置"
      top="4vh"
      width="82vw"
      v-model="open"
      @closed="dialogClose"
    >
      <div>
        <div class="flex gap-2">
          <el-select
            style="width: 180px"
            v-model="mapState.city"
            placeholder="Select"
          >
            <el-option
              v-for="item in provinceOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <el-autocomplete
            v-model="mapState.searchValue"
            :fetch-suggestions="querySearch"
            clearable
            class="inline-input w-50"
            placeholder="输入关键字"
            @select="handleSelect"
          >
            <template #default="{ item }">
              <div class="flex">
                <p>{{ item.name }}</p>
                <p class="text-gray-500" v-show="item.address != ''">
                  （{{ item.address }}）
                </p>
              </div>
            </template>
          </el-autocomplete>
        </div>
        <div
          class="mt-4"
          id="chooseMap"
          :style="{ width: props.width, height: props.height }"
        ></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="confirm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>
