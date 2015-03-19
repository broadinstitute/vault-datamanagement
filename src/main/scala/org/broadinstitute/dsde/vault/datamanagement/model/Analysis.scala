package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "An Analysis")
case class Analysis
(
  @(ApiModelProperty@field)(value = "The Vault IDs of the unmapped BAMs used as input for this Analysis.", required = true)
  input: List[String],

  @(ApiModelProperty@field)(value = "The metadata key-value pairs associated with this Analysis.", required = true)
  metadata: Map[String, String],

  @(ApiModelProperty@field)(value = "The output files associated with this Analysis, each with a unique user-supplied string key.", required = false)
  files: Option[Map[String, String]] = None,

  @(ApiModelProperty@field)(value = "The Vault ID of this Analysis", required = false)
  id: Option[String] = None
  )
