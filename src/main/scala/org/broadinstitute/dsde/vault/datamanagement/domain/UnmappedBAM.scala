package org.broadinstitute.dsde.vault.datamanagement.domain

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "An unmapped BAM")
case class UnmappedBAM
(
  @(ApiModelProperty@field)(value = "The files associated with this unmapped BAM, each with a unique user-supplied string key.", required = true)
  files: Map[String, String],

  @(ApiModelProperty@field)(value = "The metadata key-value pairs associated with this unmapped BAM.", required = true)
  metadata: Map[String, String],

  @(ApiModelProperty@field)(value = "The Vault ID of this unmapped BAM", required = false)
  id: Option[String] = None
  )
