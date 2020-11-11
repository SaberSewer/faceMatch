package online.cangjie.face.controller;

import online.cangjie.face.entity.ReqImg;
import online.cangjie.face.entity.Result;
import online.cangjie.face.service.FaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

@RestController
public class FaceController {
    protected Logger logger = LoggerFactory.getLogger(FaceController.class);
    @Resource
    private FaceService faceService;
    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping(value = "faceMatch")
    public Result faceMatch(@RequestParam("image") MultipartFile multipartFile) {
        return Result.success("aaa");
    }

    @PostMapping(value = "face")
    public Result face(@RequestBody ReqImg reqImg) throws IOException {
        logger.info(reqImg.getImage());
       return faceService.matchFace(reqImg);
    }

    @PutMapping(value = "save")
    public Result save(@RequestBody ReqImg reqImg) throws IOException {
        logger.info(reqImg.getImage());
        return faceService.save(reqImg);
    }

}
