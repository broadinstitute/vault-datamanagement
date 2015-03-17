package org.broadinstitute.dsde.vault.datamanagement.domain

case class RelationKeyValue(entityType: String, attributeName: String)

object RelationKeyValue {
  val FILE_TYPE = RelationKeyValue("fileType", "name")
  val FILE_PATH = RelationKeyValue("file", "path")
}
