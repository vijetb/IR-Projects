package com.vijet.ir;

import java.net.InetAddress;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.google.gson.Gson;
import com.vijet.ir.elasticsearchclient.ESClient;
import com.vijet.ir.loadfiles.IndexDocuments;
import com.vijet.ir.model.DOC;


public class CopyOfAppStartup {

	public static void main(String[] args) throws Exception {
		final String FOLDER_PATH = "ap89_collection";

		
		
		Settings settings = Settings.settingsBuilder()
		        .put("cluster.name", "Vijet-MAC_dev")
		        .put("client.transport.ping_timeout","129600s").build();
		
		Client client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"), 9300));
		
		DOC doc = new DOC();
		doc.setDOCNO("1");
		doc.setTEXT("HEHEH");
		IndexResponse response = client.prepareIndex("ap_dataset", "document")
		        .setSource(new Gson().toJson(doc,DOC.class))
		        .get();
		System.out.println(response.toString());
	}

}
