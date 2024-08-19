package com.xlm.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class Sku {
    //商品主键id
    private String id;
    //商品名称
    private String name;
    //价格
    private Integer price;
    //库存数量
    private Integer num;
    //图⽚
    private String image;
    // 分类名称
    private String categoryName;
    //品牌名称
    private String brandName;
    //规格
    private String spec;
    //销量
    private Integer saleNum;

    public Sku(String id, String name, Integer price, Integer num, String image, String categoryName, String brandName, String spec, Integer saleNum) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.num = num;
        this.image = image;
        this.categoryName = categoryName;
        this.brandName = brandName;
        this.spec = spec;
        this.saleNum = saleNum;
    }

    public static List<Sku> generateRandomSkus(int count) {
        List<Sku> skus = new ArrayList<>();
        Random random = new Random();

        String[] names = {"Galaxy S21", "iPhone 13", "Pixel 6", "一加9", "Xperia 5", "小米11", "红米Note 10", "Oppo Find X3", "华为P40", "Realme GT"};
        String[] brands = {"三星", "苹果", "谷歌", "一加", "索尼", "小米", "红米", "Oppo", "华为", "Realme"};
        String[] specs = {"8GB内存, 128GB存储", "6GB内存, 64GB存储", "12GB内存, 256GB存储", "4GB内存, 32GB存储", "16GB内存, 512GB存储"};
        String categoryName = "智能手机";

        for (int i = 0; i < count; i++) {
            String id = "SKU" + (i + 1);
            String name = names[random.nextInt(names.length)];
            Integer price = 5000 + random.nextInt(10000); // 价格在5000到15000之间
            Integer num = 10 + random.nextInt(90);        // 库存数量在10到100之间
            String image = "image" + (i + 1) + ".jpg";
            String brandName = brands[random.nextInt(brands.length)];
            String spec = specs[random.nextInt(specs.length)];
            Integer saleNum = random.nextInt(5000);       // 销量在0到5000之间

            Sku sku = new Sku(id, name, price, num, image, categoryName, brandName, spec, saleNum);
            skus.add(sku);
        }

        return skus;
    }

}