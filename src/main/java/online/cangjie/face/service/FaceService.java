package online.cangjie.face.service;

import online.cangjie.face.entity.ReqImg;
import online.cangjie.face.entity.Result;

import java.io.IOException;

public interface FaceService {
    void matchImage(byte[] img1, byte[] img2);

    Result matchFace(ReqImg reqImg) throws IOException;

    Result save(ReqImg reqImg) throws IOException;
}
