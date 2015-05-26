package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.model._
import org.broadinstitute.dsde.vault.datamanagement.model.GenericEntityIngest
import org.broadinstitute.dsde.vault.datamanagement.model.GenericRelationshipIngest
import org.broadinstitute.dsde.vault.datamanagement.model.GenericIngest

import spray.json.DefaultJsonProtocol

object JsonImplicits extends DefaultJsonProtocol {
  // via https://github.com/jacobus/s4/blob/8dc0fbb04c788c892cb93975cf12f277006b0095/src/main/scala/s4/rest/S4Service.scala
  implicit val impUnmappedBAM = jsonFormat4(UnmappedBAM)
  implicit val impAnalysis = jsonFormat5(Analysis)
  implicit val impBAMCollection = jsonFormat4(UBamCollection)
  implicit val impEntitySearchResult = jsonFormat2(EntitySearchResult)
  implicit val impGenericSysAttrs = jsonFormat5(GenericSysAttrs)
  implicit val impGenericEntity = jsonFormat4(GenericEntity)
  implicit val impGenericRelationship = jsonFormat2(GenericRelationship)
  implicit val impGenericRelEnt = jsonFormat2(GenericRelEnt)
  implicit val impGenericEntityIngest = jsonFormat3(GenericEntityIngest)
  implicit val impGenericRelationshipIngest = jsonFormat4(GenericRelationshipIngest)
  implicit val impGenericIngest = jsonFormat2(GenericIngest)
  implicit val impGenericAttributeSpec = jsonFormat2(GenericAttributeSpec)
  implicit val impGenericQuery = jsonFormat3(GenericEntityQuery)
  implicit val impTermSearch = jsonFormat2(TermSearch)
  implicit val impSearchResponse = jsonFormat1(IndexResponse)

}
