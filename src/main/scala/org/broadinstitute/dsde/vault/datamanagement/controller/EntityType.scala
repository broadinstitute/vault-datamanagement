package org.broadinstitute.dsde.vault.datamanagement.controller

case class EntityType(databaseKey: String, endpoint: String)

object EntityType {
  val UNMAPPED_BAM = EntityType("unmappedBAM", "ubam")
  val ANALYSIS = EntityType("analysis", "analyses")
  val TYPES = Seq(UNMAPPED_BAM, ANALYSIS)
}
