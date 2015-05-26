package org.broadinstitute.dsde.vault.datamanagement.searchengine

import java.util.concurrent.TimeUnit

import org.broadinstitute.dsde.vault.datamanagement.DataManagementConfig.ElasticSearchConfig
import org.broadinstitute.dsde.vault.datamanagement.model.{IndexResponse, TermSearch}
import org.elasticsearch.action.bulk.{BulkRequestBuilder, BulkResponse}
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search._
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.{ImmutableSettings, Settings}
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.indices.IndexMissingException
import org.elasticsearch.search.SearchHit

import scala.collection.mutable.HashMap
import scala.util.parsing.json.JSONObject



class SearchEngineClient {

  var settings: Settings = ImmutableSettings.settingsBuilder()
    .put("cluster.name", ElasticSearchConfig.clusterName).build();

  var client : Client = new TransportClient(settings)
    .addTransportAddress(new InetSocketTransportAddress(ElasticSearchConfig.server, ElasticSearchConfig.port.toInt))

  def indexMetadata(metadataList:HashMap[String,Map[String, String]],  entityType:String): IndexResponse = {
    var response: IndexResponse = null
    if(!metadataList.isEmpty){
        val bulkRequest: BulkRequestBuilder  = client.prepareBulk();
        val indexRequestList: Iterable[IndexRequest] = for ((k, v) <- metadataList) yield {
          new IndexRequest(ElasticSearchConfig.indexName.toString, entityType, k).source(JSONObject.apply(v).toString())
        }
       for(indexRequest <- indexRequestList) bulkRequest.add(indexRequest)
       bulkRequest.setRefresh(true)
       val bulkResponse: BulkResponse = bulkRequest.execute().actionGet();
        bulkResponse.getTookInMillis
       if (bulkResponse.hasFailures()) {
          response = IndexResponse(bulkResponse.buildFailureMessage())
       }else {
         response = new IndexResponse("Index  successful")
       }
    }else{
      response = new IndexResponse("Does not exist metadata")
    }
    response
  }

  def searchIdList(terms:List[TermSearch], entityType:String): List[String] = {
    var idList: List[String] = Nil
    try{
      val qb: BoolQueryBuilder  = QueryBuilders.boolQuery()
      for (t <- terms) {
        qb.should(QueryBuilders.matchQuery(t.key, t.value))
      }
      var scrollResp: SearchResponse = client.prepareSearch(ElasticSearchConfig.indexName.toString)
        .setTypes(entityType)
        .setSearchType(SearchType.SCAN)
        .setScroll(new TimeValue(1,TimeUnit.MINUTES))
        .setQuery(qb)
        //100 hits per shard will be returned for each scroll
        .setSize(100).execute().actionGet();
      //Scroll until no hits are returned
      var hasResponse: Boolean = true
      while (hasResponse) {
        for (hit: SearchHit  <- scrollResp.getHits().getHits()) {
          idList = idList :+   hit.getId
        }
        scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(600000)).execute().actionGet();
        if (scrollResp.getHits().getHits().length == 0) {
          hasResponse = false
        }
      }
    }catch{
      case ioe: IndexMissingException => idList
    }
   idList
  }


}

