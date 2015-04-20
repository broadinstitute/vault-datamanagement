package org.broadinstitute.dsde.vault.datamanagement.services

import org.broadinstitute.dsde.vault.datamanagement.model._
import spray.json.DefaultJsonProtocol

object JsonImplicits extends DefaultJsonProtocol {
  // via https://github.com/jacobus/s4/blob/8dc0fbb04c788c892cb93975cf12f277006b0095/src/main/scala/s4/rest/S4Service.scala
  implicit val impUnmappedBAM = jsonFormat4(UnmappedBAM)
  implicit val impAnalysis = jsonFormat5(Analysis)
  implicit val impBAMCollection = jsonFormat3(UBamCollection)
  implicit val impEntitySearchResult = jsonFormat2(EntitySearchResult)
}
