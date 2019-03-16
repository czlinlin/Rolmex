package com.mossle.common.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.activation.DataSource;
import javax.annotation.Resource;

import org.activiti.engine.impl.util.json.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import com.mossle.api.store.StoreConnector;
import com.mossle.api.store.StoreDTO;
import com.mossle.core.store.MultipartFileDataSource;
import com.mossle.internal.store.persistence.domain.StoreInfo;

public class FileUploadAPI {

    private WebAPI webAPI;
    private StoreConnector storeConnector;


    public void uploadFile(MultipartFile[] files, String tenantId, String pkId, String model)
            throws Exception, IOException {
        uploadFile(files, tenantId, pkId, model, "0");
    }


    public void uploadFile(MultipartFile[] files, String tenantId, String pkId, String model, String stoType)
            throws Exception, IOException {
        // 判断file数组不能为空并且长度大于0
        if (files != null && files.length > 0) {
            // 循环获取file数组中得文件
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                // 保存文件
                if (!file.isEmpty()) {
                    DataSource dataSource = new MultipartFileDataSource(file);
                    String suffix = FileUtils.getSuffix(dataSource.getName());
                    suffix = suffix.toLowerCase(Locale.forLanguageTag(suffix));
                    String imgSrc = "";
                    try {
                        imgSrc = webAPI.upload2WWW(FileUtils.multipartToFile(file),
                                "?folderName=cms&suffix_name=" + suffix + "&params=" + model);
                        System.out.println("==================上传成功,返回数据[" + imgSrc + "]==========================");

                    } catch (Exception e) {
                        System.out
                                .println("==================上传失败,返回数据[" + e.toString() + "]==========================");
                    }

                    JSONObject jsonObject = new JSONObject(imgSrc);
                    String path = jsonObject.getString("source");

                    StoreDTO storeDto = storeConnector.saveStore(model, dataSource, tenantId, pkId, path, stoType);
                }
            }
        }
    }

    /*公告用上传文件*/
    public String uploadFile(MultipartFile file, String model)
            throws Exception, IOException {
        String path = null;
        // 保存文件
        if (!file.isEmpty()) {
            DataSource dataSource = new MultipartFileDataSource(file);
            String suffix = FileUtils.getSuffix(dataSource.getName());
            suffix = suffix.toLowerCase(Locale.forLanguageTag(suffix));
            String imgSrc = "";
            try {
                imgSrc = webAPI.upload2WWW(FileUtils.multipartToFile(file),
                        "?folderName=cms&suffix_name=" + suffix + "&params=" + model);
                System.out.println("==================上传成功,返回数据[" + imgSrc + "]==========================");

            } catch (Exception e) {
                System.out
                        .println("==================上传失败,返回数据[" + e.toString() + "]==========================");
            }
            JSONObject jsonObject = new JSONObject(imgSrc);
            path = jsonObject.getString("source");
        }
        return path;
    }

    /**
     * 通过文件（路径）写入 store_info Bing 2017.9.28
     *
     * @param path     文件路径
     * @param tenantId
     * @param pkId     外键（汇报、任务、项目等的id）
     * @param model    功能模块（OA/worktask，OA/report，OA/project。。。）
     * @throws Exception
     * @throws IOException
     */
    public void uploadFile(String path, String tenantId, String pkId, String model) throws Exception, IOException {
        File file = new File(path);
        String fileName = file.getName();
        storeConnector.saveStore(model, fileName, tenantId, pkId, path);
    }

    public void uploadFile(String path, String tenantId, String pkId, String model, String stoType)
            throws Exception, IOException {
        File file = new File(path);
        String fileName = file.getName();
        storeConnector.saveStore(model, fileName, tenantId, pkId, path, stoType);
    }

    public void uploadFileCopy(String pkPriId, String strNewPkId, String model, String iptdels)
            throws Exception, IOException {
        /*
         * StoreDTO saveStore(String model, String fileName, String tenantId,
         * String pkId,String path)
         */
        if (iptdels == null)
            iptdels = "";
        List<StoreInfo> list = getStore(model, pkPriId);
        if (list != null && list.size() > 0) {
            for (StoreInfo store : list) {
                if (!iptdels.contains(store.getPath())) {
                    storeConnector.saveStore(model, store.getName(), store.getTenantId(), strNewPkId, store.getPath(), "0");
                }

                // StoreInfo storeNew=store;
                // storeNew.setPkId("");
            }
        }
    }

    public void uploadFileDel(String delfiles, String pkId) throws Exception, IOException {

        // 先删除过往（已标记的）
        if (delfiles != null && !delfiles.equals("") && delfiles.split(",").length > 0) {
            String[] delfileList = delfiles.split(",");
            for (int i = 0; i < delfileList.length; i++) {
                storeConnector.removeStore(delfileList[i], pkId);
            }
        }
    }

    public List<StoreInfo> getStore(String model, String pkId) throws Exception {
        List<StoreInfo> list = storeConnector.getStore(model, pkId);
        return list;
    }

    public List<StoreInfo> getStoreByType(String model, String pkId, String stoType) throws Exception {
        List<StoreInfo> list = storeConnector.getStoreByType(model, pkId, stoType);
        return list;
    }

    public List<StoreInfo> getStore(String pkId) throws Exception {
        List<StoreInfo> list = storeConnector.getStore(pkId);
        return list;
    }

    public List<StoreInfo> getStoreByType(String pkId, String stoType) throws Exception {
        List<StoreInfo> list = storeConnector.getStoreByType(pkId, stoType);
        return list;
    }

    public void removeStore(List<StoreInfo> listStoreInfo) throws Exception, IOException {
        for (StoreInfo si : listStoreInfo) {
            storeConnector.removeStore(si.getPath(), si.getPkId());
        }
    }

    public void removeStoreByType(String path, String pkId, String stoType) throws Exception, IOException {
        storeConnector.removeStoreByType(path, pkId, stoType);
    }

    @Resource
    public void setWebAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
    }

    @Resource
    public void setStoreConnector(StoreConnector storeConnector) {
        this.storeConnector = storeConnector;
    }

}
