package com.leyou.goods.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;

    @Autowired
    private GoodsService goodsService;

    /**
     * 获取静态页面
     * @param supId
     */
    public void createHtml(Long supId){

        //获取thymeleaf运行上下文对象context
        Context context = new Context();

        //设置数据模型
        context.setVariables(goodsService.loadData(supId));
        PrintWriter printWriter = null;

        try {
            //把静态文件生成到服务器本地
             File file = new File("D:\\nginx-1.16.1\\html\\item\\" + supId + ".html");
             printWriter = new PrintWriter(file);

            engine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (printWriter!=null){
                printWriter.close();
            }
        }

    }

    /**
     * 删除静态页面
     * @param id
     */
    public void deleteHtml(Long id) {
        File file = new File("D:\\nginx-1.16.1\\html\\item\\" + id + ".html");
            file.deleteOnExit();
    }
}
