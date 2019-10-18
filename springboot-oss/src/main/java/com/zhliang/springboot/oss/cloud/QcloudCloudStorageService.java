package com.zhliang.springboot.oss.cloud;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.request.UploadFileRequest;
import com.qcloud.cos.sign.Credentials;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * @Author: colin
 * @Date: 2019/8/20 19:31
 * @Description:
 * @Version: V1.0
 */
public class QcloudCloudStorageService extends CloudStorageService {

    private COSClient client;

    public QcloudCloudStorageService (CloudStorageConfig config){
        this.config = config;
        //初始化
        init();
    }

    private void init(){
        Credentials credentials = new Credentials(config.getQcloudAppId(), config.getQcloudSecretId(), config.getQcloudSecretKey());
        //初始化客户端配置
        ClientConfig clientConfig = new ClientConfig();
        //设置bucket所在的区域，华南：gz 华北：tj 华东：sh
        clientConfig.setRegion(config.getQcloudRegion());

        client = new COSClient(clientConfig,credentials);
    }

    @Override
    public String upload(byte[] data, String path) {
        //腾讯云 必须以"/"开头
        if(!path.startsWith("/")){
            path = "/" + path;
        }

        //上传到腾讯云
        UploadFileRequest request = new UploadFileRequest(config.getQcloudBucketName(),path,data);
        String response = client.uploadFile(request);
        JSONObject object = JSONObject.fromObject(response);
        if(object.getInt("code") != 0){
            throw new RuntimeException("文件上传失败" + object.getString("message"));
        }
        return config.getQcloudDomain() + path;
    }

    @Override
    public String uploadSuffix(byte[] data, String suffix) {
        return upload(data, getPath(config.getQcloudPrefix(),suffix));
    }

    @Override
    public String upload(InputStream inputStream, String path) {
        try{
            byte[] data = IOUtils.toByteArray(inputStream);
            return this.upload(data, path);
        }catch (Exception e){
            throw new RuntimeException("上传文件失败" + e.getMessage());
        }
    }

    @Override
    public String uploadSuffix(InputStream inputStream, String suffix) {
        return upload(inputStream, getPath(config.getQcloudPrefix(),suffix));
    }
}
