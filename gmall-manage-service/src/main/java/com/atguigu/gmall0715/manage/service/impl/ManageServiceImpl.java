package com.atguigu.gmall0715.manage.service.impl;




import org.apache.commons.lang3.StringUtils;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0715.bean.*;
import com.atguigu.gmall0715.config.RedisUtil;
import com.atguigu.gmall0715.manage.constant.ManageConst;
import com.atguigu.gmall0715.manage.mapper.*;
import com.atguigu.gmall0715.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2);
        return baseCatalog2List;
    }



    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);

        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;

    }

    @Override
    public List<BaseAttrInfo> getAttrList(BaseAttrInfo baseAttrInfo) {
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if(baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else{
            //保存平台属性
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        //先删除再新增
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();

        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);
        //保存平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(attrValueList != null && attrValueList.size() > 0){
            for(BaseAttrValue baseAttrValue : attrValueList){
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(String attrId) {

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);

        //查询平台属性值结合
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);

        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> selectBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        //判断为空保存，不为空修改 suoinfo
        if (spuInfo.getId()==null || spuInfo.getId().length()==0){
            //保存数据
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else{
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }
        //spuImage图片列表，先删除再新增
        //delete from spuimage where spuID = ？
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        //保存数据，先获取数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()>0){
            //遍历
            for (SpuImage spuimage:spuImageList) {
                spuimage.setId(null);
                spuimage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuimage);
            }
        }
        //销售属性删除，然后插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        //销售属性值删除，然后插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        //获取数据，添加属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            //遍历
            for (SpuSaleAttr saleAttr:spuSaleAttrList){
                saleAttr.setId(null);
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(saleAttr);
                //添加属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList!=null && spuSaleAttrValueList.size()>0){
                    //遍历
                    for (SpuSaleAttrValue saleAttrValue:spuSaleAttrValueList){
                        saleAttrValue.setId(null);
                        saleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insert(saleAttrValue);
                    }
                }
            }


        }

    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId) {
        return  spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        return baseAttrInfoMapper.selectBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //skuinfo 判断skuid是否为空，是则插入，否则修改
        if (skuInfo.getId()==null || skuInfo.getId().length()==0){
            //设置id为自增
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //sku img 先删除原有sku图片
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        //insert 然后插入现有图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList!=null && skuImageList.size()>0){
            for (SkuImage image:skuImageList) {
                //区别null
                if (image.getId()!=null && image.getId().length()==0){
                    image.setId(null);
                }
                //skuid必须赋值
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }

        //sku_attr_value 先删除原有属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        //插入数据 插入传入的属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue attrValue:skuAttrValueList) {
                if (attrValue.getId()!=null && attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                //skuid
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
        //sku_sale_attr_value 删除原有消费属性值
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        //插入数据 插入消费属性值
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue saleAttrValue:skuSaleAttrValueList) {
                if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }

                //skuid
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {
        //测试redis
        Jedis jedis = null;
        SkuInfo skuInfo = null;
        try {
            jedis = redisUtil.getJedis();
            //定义key
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;

            String skuInfoJson = jedis.get(skuInfoKey);

            if (skuInfoJson == null || skuInfoJson.length() == 0) {
                //没有数据，需要加锁，取出完数据，放入缓存，下次直接从缓存中取
                System.out.println("没有命中缓存");
                //定义key
                String skuLockKey = ManageConst.SKUKEY_PREFIX + skuId + ManageConst.SKUKEY_SUFFIX;
                //生成锁
                String lockKey = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)) {
                    System.out.println("获取锁");
                    //从数据库中取得数据
                    skuInfo = getSkuInfoDB(skuId);
                    //将数据放入缓存
                    //将对象转换成字符串
                    String jsonString = JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, jsonString);
                    jedis.close();
                    return skuInfo;
                }else{
                    System.out.println("wait!");
                    //wait
                    Thread.sleep(1000);
                    //自旋
                    return getSkuInfo(skuId);
                }
            }else {
                //有数据
                skuInfo = JSON.parseObject(skuInfoJson,SkuInfo.class);
                jedis.close();
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getSkuInfoDB(skuId);

    }

    public SkuInfo getSkuInfoDB(String skuId){
        //单纯的信息
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        //查询图片信息
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> select = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(select);
        //skuAttrValue信息
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValues);

        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        //根据spuid组成map
        List<Map> mapList =skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);
        HashMap<Object, Object> hashMap = new HashMap<>();
        for (Map map:mapList) {
            hashMap.put(map.get("value_ids"),map.get("sku_id"));
        }
        return hashMap;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(),",");
        System.out.println("传入的字符串"+attrValueIds);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }
}