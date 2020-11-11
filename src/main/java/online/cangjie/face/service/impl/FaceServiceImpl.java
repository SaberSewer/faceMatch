package online.cangjie.face.service.impl;

import com.arcsoft.face.*;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectOrient;
import com.arcsoft.face.toolkit.ImageInfo;
import online.cangjie.face.entity.ReqImg;
import online.cangjie.face.entity.Result;
import online.cangjie.face.service.FaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.arcsoft.face.toolkit.ImageFactory.getRGBData;

@Service("faceService")
public class FaceServiceImpl implements FaceService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RedisTemplate redisTemplate;
    @Value("${faceLib}")
    private String path;

    public void matchImage(byte[] img1, byte[] img2) {
        ImageInfo info1 = getRGBData(img1);
        ImageInfo info2 = getRGBData(img2);
        FaceEngine  faceEngine = getFaceEngine();
        int errorCode = 0;
        //对照片1进行识别
        List<FaceInfo> faceInfoList = new ArrayList<>();
        errorCode = faceEngine.detectFaces(info1.getImageData(), info1.getWidth(), info1.getHeight(), info1.getImageFormat(), faceInfoList);
        System.out.println(errorCode);
        System.out.println(faceInfoList);
        //获取该图片的特征码
        FaceFeature faceFeature = new FaceFeature();
        faceEngine.extractFaceFeature(info1.getImageData(), info1.getWidth(), info1.getHeight(), info1.getImageFormat(), faceInfoList.get(0), faceFeature);
        FaceFeature targetFaceFeature = new FaceFeature();
        targetFaceFeature.setFeatureData(faceFeature.getFeatureData());
        //对照片2进行处理
        List<FaceInfo> faceInfoList2 = new ArrayList<FaceInfo>();
        faceEngine.detectFaces(info2.getImageData(), info2.getWidth(), info2.getHeight(), info2.getImageFormat(), faceInfoList2);
        System.out.println(faceInfoList);
        faceInfoList2.forEach(faceInfo -> {
            FaceFeature faceFeature2 = new FaceFeature();
            faceEngine.extractFaceFeature(info2.getImageData(), info2.getWidth(), info2.getHeight(), info2.getImageFormat(), faceInfo, faceFeature2);
            FaceFeature sourceFaceFeature = new FaceFeature();
            sourceFaceFeature.setFeatureData(faceFeature2.getFeatureData());
            FaceSimilar faceSimilar = new FaceSimilar();
            faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
            System.out.println("相似度：" + faceSimilar.getScore());
        });
    }

    @Override
    public Result matchFace(ReqImg reqImg) throws IOException {
        byte[] bytes = base64ToBytes(reqImg.getImage());
        ImageInfo info1 = getRGBData(bytes);
        FaceEngine faceEngine = getFaceEngine();
        //如果需要同时使用两种不同的模式需要创建两个引擎
        int errorCode = 0;
        List<FaceInfo> faceInfoList = new ArrayList<>();
        errorCode = faceEngine.detectFaces(info1.getImageData(), info1.getWidth(), info1.getHeight(), info1.getImageFormat(), faceInfoList);
        System.out.println(errorCode);
        System.out.println(faceInfoList);
        if(faceInfoList.isEmpty()) {
            return Result.fail("没有人脸信息");
        }
        FaceFeature faceFeature = new FaceFeature();
        faceEngine.extractFaceFeature(info1.getImageData(), info1.getWidth(), info1.getHeight(), info1.getImageFormat(), faceInfoList.get(0), faceFeature);
        FaceFeature sourceFaceFeature = new FaceFeature();
        sourceFaceFeature.setFeatureData(faceFeature.getFeatureData());
        //从redis中获取比对;
        byte[] check = (byte[]) redisTemplate.execute(new RedisCallback() {
            @Override
            public byte[] doInRedis(RedisConnection redisConnection) throws DataAccessException {
                if(redisConnection.exists(reqImg.getId().getBytes())) {
                    return redisConnection.get(reqImg.getId().getBytes());
                }
                return null;
            }
        });
        if(check == null) {
            return Result.fail("该用户面部信息不存在");
        }
        FaceFeature targetFaceFeature = new FaceFeature();
        targetFaceFeature.setFeatureData(check);
        FaceSimilar faceSimilar = new FaceSimilar();
        faceEngine.compareFaceFeature(targetFaceFeature, sourceFaceFeature, faceSimilar);
        System.out.println("相似度：" + faceSimilar.getScore());
        System.out.println(faceSimilar);
        return Result.successWith(faceSimilar, "成功");
    }

    @Override
    public Result save(ReqImg reqImg) throws IOException {
        FaceEngine faceEngine = getFaceEngine();
        byte[] bytes = base64ToBytes(reqImg.getImage());
        ImageInfo info1 = getRGBData(bytes);
        FaceFeature faceFeature = new FaceFeature();
        List<FaceInfo> faceInfoList = new ArrayList<>();
        int errorCode = 0;
        errorCode = faceEngine.detectFaces(info1.getImageData(), info1.getWidth(), info1.getHeight(), info1.getImageFormat(), faceInfoList);
        System.out.println(errorCode);
        System.out.println(faceInfoList);
        if(faceInfoList.isEmpty()) {
            return Result.fail("获取人脸失败");
        }
        faceEngine.extractFaceFeature(info1.getImageData(), info1.getWidth(), info1.getHeight(), info1.getImageFormat(), faceInfoList.get(0), faceFeature);
        FaceFeature targetFaceFeature = new FaceFeature();
        targetFaceFeature.setFeatureData(faceFeature.getFeatureData());
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.set(reqImg.getId().getBytes(), targetFaceFeature.getFeatureData());
                return null;
            }
        });
        return Result.success("aa");
    }

    private FaceEngine getFaceEngine() {
        FaceEngine faceEngine = new FaceEngine(path);
        EngineConfiguration engineConfiguration = new EngineConfiguration();
        //照片模式-使用推荐配置
        //engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_VIDEO);
        engineConfiguration.setDetectMode(DetectMode.ASF_DETECT_MODE_IMAGE);
        engineConfiguration.setDetectFaceOrientPriority(DetectOrient.ASF_OP_ALL_OUT);
        engineConfiguration.setDetectFaceMaxNum(10);
        engineConfiguration.setDetectFaceScaleVal(16);
        FunctionConfiguration functionConfiguration = new FunctionConfiguration();
        //年龄识别
        functionConfiguration.setSupportAge(true);
        //3D检测
        functionConfiguration.setSupportFace3dAngle(true);
        //人脸检测
        functionConfiguration.setSupportFaceDetect(true);
        //人脸识别
        functionConfiguration.setSupportFaceRecognition(true);
        //性别
        functionConfiguration.setSupportGender(true);
        //活体检测
        functionConfiguration.setSupportLiveness(true);
        //活体检测
        functionConfiguration.setSupportIRLiveness(true);
        engineConfiguration.setFunctionConfiguration(functionConfiguration);
        int errorCode = faceEngine.init(engineConfiguration);
        return faceEngine;
    }

    private byte[] base64ToBytes(String base64Image) throws IOException {
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] bytes = decoder.decodeBuffer(base64Image);
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i] < 0) {// 调整异常数据
                bytes[i] += 256;
            }
        }
        return bytes;
    }
}
