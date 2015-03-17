package org.broadinstitute.dsde.vault.datamanagement.domain

case class RelationKeyValue(entityType: String, attributeName: String)

object RelationKeyValue {
  val FILE_TYPE = RelationKeyValue("filyType", "name")
  val FILE_PATH = RelationKeyValue("file", "path")
}
