package com.yitong.guides.web.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UploadController {

  private static final Logger log = LoggerFactory.getLogger(UploadController.class);

  /** 允许的图片扩展名（小写），用于过滤 contentType 解析结果，避免非法路径 */
  private static final Set<String> ALLOWED_IMAGE_EXT =
      Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "ico");

  @Value("${app.upload.dir:./uploads}")
  private String uploadDir;

  @PostMapping("/upload")
  public UploadResp upload(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "请选择文件");
    }
    String contentType = file.getContentType();
    if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅支持图片");
    }
    // 去掉参数部分，例如 image/jpeg;charset=utf-8 -> image/jpeg
    String mainType = contentType.split(";")[0].trim().toLowerCase();
    if (!mainType.startsWith("image/")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "仅支持图片");
    }
    String ext = mainType.substring("image/".length());
    if (ext.isEmpty() || !ext.matches("[a-z]+")) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "不支持的图片类型");
    }
    if ("jpeg".equals(ext)) {
      ext = "jpg";
    }
    if (!ALLOWED_IMAGE_EXT.contains(ext)) {
      ext = "jpg";
    }
    String filename = UUID.randomUUID().toString() + "." + ext;
    try {
      Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
      Files.createDirectories(dir);
      Path target = dir.resolve(filename);
      file.transferTo(target);
      return new UploadResp("/uploads/" + filename);
    } catch (IOException e) {
      log.error("上传失败: {}", e.getMessage(), e);
      throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "上传失败");
    }
  }

  public record UploadResp(String url) {}
}
