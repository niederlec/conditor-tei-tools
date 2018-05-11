<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:hal="http://hal.archives-ouvertes.fr" 
    xmlns="http://www.tei-c.org/ns/1.0"
    xpath-default-namespace="http://www.tei-c.org/ns/1.0"
    exclude-result-prefixes="xs hal"
    version="2.0">
    
    <xsl:output indent="yes"/>
    
    
    <xsl:template match="abstract">
        
        <xsl:if test="list or p or table"><abstract><xsl:apply-templates/></abstract></xsl:if>
        <xsl:if test="not(list or p or table)">
            <abstract><p><xsl:apply-templates/></p></abstract>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="biblFull">
        <biblFull>
            <xsl:apply-templates select="editionStmt"/>
            <xsl:apply-templates select="sourceDesc"/>
            <xsl:apply-templates select="profileDesc"/>
        </biblFull>
    </xsl:template>
    
    <xsl:template match="editionStmt">
        <editionStmt>
            <xsl:apply-templates select="edition"/>
        </editionStmt>
    </xsl:template>
    
    <xsl:template match="edition">
        <edition>
          <date type="whenDownloaded"><xsl:value-of select="substring(/TEI/teiHeader/fileDesc/publicationStmt/date/@when,0,11)"/></date>
          <xsl:if test="date[@type='whenProduced']">
              <date type="whenCreated"><xsl:value-of  select="format-date(current-date(),'[Y0001]-[M01]-[D01]')"/></date>
          </xsl:if>
        </edition>
    </xsl:template>
    
    <xsl:template match="idno[@type='halauthorid']">
        <idno type="halAuthorId"><xsl:apply-templates/></idno>
    </xsl:template>
    
    <xsl:template match="monogr">
        <monogr>
            <xsl:apply-templates></xsl:apply-templates>
        </monogr>
        <idno type='halId'><xsl:value-of select="substring(string(/TEI/teiHeader/fileDesc/titleStmt/title),19)"/></idno>
    </xsl:template>
    
    <xsl:template match="classCode[@scheme='halTypology']">
        <classCode scheme="typology"><xsl:value-of select="@n"/></classCode>
    </xsl:template>
    
    <!-- ne pas reporter les éléments suivants -->
    <xsl:template match="teiHeader">
        <teiHeader>
            <fileDesc>
                <titleStmt>
                    <title>Conditor platform version of <xsl:value-of select="/TEI/text/body/listBibl/biblFull/publicationStmt/idno[@type='halId']"/> document. This version derivate from the HAL original version.</title>
                </titleStmt>
                <publicationStmt>
                    <distributor>Conditor</distributor>
                    <availability status="restricted">
                        <p>Access limited to granted organizations</p>
                        <!--<licence target="http://creativecommons.org/licenses/by/4.0/">Distributed under a Creative Commons Attribution 4.0 International License</licence>-->
                    </availability>
                    <date when="{format-date(current-date(),'[Y0001]-[M01]-[D01]')}"/>
                </publicationStmt>
                <sourceDesc>
                    <p part="N">Conditor platform</p>
                </sourceDesc>
            </fileDesc>
        </teiHeader>
    </xsl:template>
    
    <xsl:template match="back"></xsl:template>
    
    <!-- ne pas reporter les éléments dans l'espace de nommage hal -->
    <xsl:template match="hal:*"></xsl:template>
    
    <xsl:template match="@status">
        <xsl:if test="string(.) = 'free' or string(.) = 'unknown' or string(.) = 'restricted'"><xsl:apply-templates/></xsl:if>
    </xsl:template>
    
    <xsl:template match="@scheme">
        <xsl:choose>
            <xsl:when test="string(.) = 'halTypology'"><xsl:attribute name="scheme">typology</xsl:attribute></xsl:when>
            <xsl:otherwise><xsl:copy><xsl:apply-templates select="."/></xsl:copy></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    
</xsl:stylesheet>