package org.broadinstitute.dsde.vault.datamanagement.model

import com.wordnik.swagger.annotations.{ApiModel, ApiModelProperty}
import scala.annotation.meta.field

@ApiModel("system-generated entity attributes")
case class GenericSysAttrs(
  @(ApiModelProperty@field)("optional BOSS ID")
  bossID: Option[String],
  @(ApiModelProperty@field)("when the entity was created (mSecs since epoch)")
  createdDate: Long,
  @(ApiModelProperty@field)("openAM commonName of entity creator")
  createdBy: String,
  @(ApiModelProperty@field)("when the entity was most recently modified (mSecs since epoch)")
  modifiedDate: Option[Long],
  @(ApiModelProperty@field)("openAM commonName of most recent modifier")
  modifiedBy: Option[String] )

@ApiModel("something that lives in the vault")
case class GenericEntity(
  @(ApiModelProperty@field)("the globally unique vault ID for the entity")
  guid: String,
  @(ApiModelProperty@field)("the entity type (open vocabulary - not enumerated)")
  entityType: String,
  @(ApiModelProperty@field)("some system-generated 'when's and 'who's")
  sysAttrs: GenericSysAttrs,
  @(ApiModelProperty@field)("an open set of metadata attributes of the entity")
  attrs: Map[String,String] )

@ApiModel("a directed relationship between two entities")
case class GenericRelationship(
  @(ApiModelProperty@field)("the type of relationship (open vocabulary - not enumerated)")
  relationType: String,
  @(ApiModelProperty@field)("an open set of metadata attributes of the relationship")
  attrs: Map[String,String] )

@ApiModel("a description of a relationship and of the entity targeted by the relationship")
case class GenericRelEnt(
  @(ApiModelProperty@field)("the relationship")
  relationship: GenericRelationship,
  @(ApiModelProperty@field)("the entity so-related")
  entity: GenericEntity )


@ApiModel("a new entity to create")
case class GenericEntityIngest(
  @(ApiModelProperty@field)("the entity type (open vocabulary - not enumerated)")
  entityType: String,
  @(ApiModelProperty@field)("optional BOSS ID")
  bossID: Option[String],
  @(ApiModelProperty@field)("an open set of metadata attributes of the entity")
  attrs: Map[String,String] )

@ApiModel("a new relationship to create")
case class GenericRelationshipIngest(
  @(ApiModelProperty@field)("the type of relationship (open vocabulary - not enumerated)")
  relationType: String,
  @(ApiModelProperty@field)("the vault ID of the upstream entity (or a reference of the form $0, $1, $2... to refer to an entity that will be created as a part of this ingest)")
  ent1: String,
  @(ApiModelProperty@field)("the vault ID of the downstream entity (or a reference of the form $0, $1, $2... to refer to an entity that will be created as a part of this ingest)")
  ent2: String,
  @(ApiModelProperty@field)("an open set of metadata attributes of the relationship")
  attrs: Map[String,String] )

@ApiModel("a small chunk of a client's object model to be created all at once")
case class GenericIngest(
  @(ApiModelProperty@field)("some entities to create")
  entities: Option[List[GenericEntityIngest]],
  @(ApiModelProperty@field)("some relationships to create among the new (or previously existing) entities")
  relations: Option[List[GenericRelationshipIngest]] )

@ApiModel("a very simple query for a type and metadata attribute value")
case class GenericQuery(
  @(ApiModelProperty@field)("the entity type")
  entityType: String,
  @(ApiModelProperty@field)("the attribute name")
  attrName: String,
  @(ApiModelProperty@field)("the attribute value")
  attrValue: String )
