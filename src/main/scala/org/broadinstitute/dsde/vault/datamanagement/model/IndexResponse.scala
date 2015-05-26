package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}

import scala.annotation.meta.field

@ApiModel(value = "Index Response.")
case class IndexResponse
(
  @(ApiModelProperty@field)(value = "The indexing result message.")
  messageResult: String


)
