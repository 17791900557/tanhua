package com.itheima.test;

import com.baidu.aip.face.AipFace;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.server.AppserverApplication;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppserverApplication.class)
public class FaceTest {


    public static final String APP_ID = "27039997";
    public static final String API_KEY = "6g59bCRfNW5VF74U8hd5cLnq";
    public static final String SECRET_KEY = "eEFM9bcu4498pHW1p0wM5cG0bApeL0Xw";

    public static void main(String[] args) {
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);


        // 调用接口
        String image = "https://tanhua921.oss-cn-hangzhou.aliyuncs.com/2022/08/16/151319a7-04d1-4600-92ec-ae4f44146387.jpg";
        String imageType = "URL";

        // 人脸检测
        JSONObject res = client.detect(image, imageType, options);
        System.out.println(res.toString(2));

        Object error_code = res.get("error_code");
        System.out.println(error_code);

    }


    @Autowired
    public AipFaceTemplate aipFaceTemplate;
    @Test
    public void FaceTest(){
        boolean detect = aipFaceTemplate.detect("https://tanhua921.oss-cn-hangzhou.aliyuncs.com/2022/08/16/151319a7-04d1-4600-92ec-ae4f44146387.jpghttps://tanhua921.oss-cn-hangzhou.aliyuncs.com/2022/08/16/151319a7-04d1-4600-92ec-ae4f44146387.jpg");
        System.out.println(detect);

    }
}
