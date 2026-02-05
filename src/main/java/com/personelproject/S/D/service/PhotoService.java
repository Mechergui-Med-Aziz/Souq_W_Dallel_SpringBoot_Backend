package com.personelproject.S.D.service;

import java.io.IOException;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
@Service
public class PhotoService {

     @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFsOperations gridFsOperations;

    // 📤 Upload photo
    public String uploadPhoto(MultipartFile file) throws IOException {
        ObjectId id = gridFsTemplate.store(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType()
        );
        return id.toString();
    }

    // 📥 Récupérer photo
    public GridFSFile getPhoto(String photoId) {
        return gridFsTemplate.findOne(
                Query.query(Criteria.where("_id").is(photoId))
        );
    }

    // 🗑️ Supprimer photo
    public void deletePhoto(String photoId) {
        gridFsTemplate.delete(
                Query.query(Criteria.where("_id").is(photoId))
        );
    }

    

    public GridFsResource getResource(GridFSFile file) {
        return gridFsOperations.getResource(file);
    }

    
}
