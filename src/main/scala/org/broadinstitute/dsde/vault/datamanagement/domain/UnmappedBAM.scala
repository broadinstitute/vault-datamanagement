package org.broadinstitute.dsde.vault.datamanagement.domain

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "An unmapped BAM")
case class UnmappedBAM
(
  @(ApiModelProperty@field)(value = "The files associated with this unmapped BAM, each with a unique user-supplied string key.", required = true)
  files: Map[String, String],

  @(ApiModelProperty@field)(value = "The metadata key-value pairs associated with this unmapped BAM.", required = true)
  metadata: Metadata,

  @(ApiModelProperty@field)(value = "The Vault ID of this unmapped BAM", required = false)
  id: Option[String] = None
  )

@ApiModel(value = "Metadata for a BAM (mapped or aggregated)")
case class Metadata
(
  @(ApiModelProperty@field)(required = true)
  ownerId: String,

  md5: Option[String] = None,

  project: Option[String] = None,

  individualAlias: Option[String] = None,

  sampleAlias: Option[String] = None,

  readGroupAlias: Option[String] = None,

  libraryName: Option[String] = None,

  sequencingCenter: Option[String] = None,

  platform: Option[String] = None,

  platformUnit: Option[String] = None,

  runDate: Option[String] = None,

  @(ApiModelProperty@field)(value = "indicates that this object supports arbitrary key-value pairs beyond the keys listed here. This is a hack right now" +
    " because I don't know the best way to represent varargs in Swagger.")
  additionalMetadata: Option[String] = None
  )
