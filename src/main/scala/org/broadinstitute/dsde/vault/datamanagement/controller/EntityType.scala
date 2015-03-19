package org.broadinstitute.dsde.vault.datamanagement.controller

case class EntityType(databaseKey: String)

object EntityType {
  val UNMAPPED_BAM = EntityType("unmappedBAM")
  val ANALYSIS = EntityType("analysis")
}
