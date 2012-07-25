package com.qunb.geosearch.geoObject;

import org.jets3t.Constants;

import com.amazon.s3shell.S3Store;

public class QunbS3Store {
	private String store;
	private String awsAccessKey;
	private String awsSecretKey;
	private S3Store service;
	public QunbS3Store(String bucket){
		this.store=Constants.S3_DEFAULT_HOSTNAME;
		this.awsAccessKey = "AKIAI7PMA6ZEGNTGS4XQ";
		this.awsSecretKey = "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";
		this.service = new S3Store(this.store,this.awsAccessKey,this.awsSecretKey);
		this.service.setBucket(bucket);
	}
	public S3Store getService(){
		return this.service;
	}

}
