package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}

import scala.annotation.meta.field

@ApiModel(value = "A collection of uBAMS")
case class UBamCollection
(
  @(ApiModelProperty@field)(value = "The Vault IDs of the uBAMs included in this collection.", required = true)
  members: Option[Seq[String]] = None,

  @(ApiModelProperty@field)(value = "The metadata key-value pairs associated with this uBAM collection.", required = true)
  metadata: Map[String, String],

  @(ApiModelProperty@field)(value = "The Vault ID of this unmapped BAM collection", required = false)
  id: Option[String] = None
  )



