package com.itheima.test;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.AppserverApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppserverApplication.class)
public class OssTest {

    @Autowired
    private OssTemplate ossTemplate;

    @Test
    public void Demo() throws FileNotFoundException {

        String Path = "C:\\Users\\李铭玥\\Desktop\\JAVA\\探花交友\\探花交友\\02-完善用户信息\\1.jpg";
        FileInputStream Is = new FileInputStream(new File(Path));
        String fileName = new SimpleDateFormat("yyyy/MM/dd").format(new Date())
                + "/" + UUID.randomUUID().toString() + Path.substring(Path.lastIndexOf("."));

        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "LTAI5tCkP11U5chV4wXBCduY";
        String accessKeySecret = "ADmeYbCi502fJuKqHETVtXejpuabLy";
        // 填写Bucket名称，例如examplebucket。
        String bucketName = "tanhua921";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写Byte数组。
//        byte[] content = "Hello OSS".getBytes();
        // 创建PutObject请求。
        ossClient.putObject(bucketName, fileName, Is);
        ossClient.shutdown();
        //下载图片的保存路径
        String url = "https://tanhua921.oss-cn-hangzhou.aliyuncs.com/" + fileName;
        System.out.println(url);
    }

    @Test
    public void OssTest() throws FileNotFoundException {
        String Path = "C:\\Users\\李铭玥\\Desktop\\JAVA\\探花交友\\探花交友\\02-完善用户信息\\1.jpg";
        FileInputStream Is = new FileInputStream(new File(Path));

        String url = ossTemplate.upload(Path, Is);
        System.out.println(url);
    }
}

