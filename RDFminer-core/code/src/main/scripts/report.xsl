<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">
  <xsl:template match="/">
    <html>
		<head><title>Candidate Axioms</title></head>
		<body>
		<h1>Candidate Axiom List</h1>
		<table border="1">
		<xsl:for-each select="//axiomTest">
		<xsl:sort data-type="number" select="./possibility" order="ascending"/>
		<xsl:sort data-type="number" select="./necessity" order="ascending"/>
		<tr  bgcolor="#9acd32">
		<th align="left">Axiom Name</th>
		<td><xsl:value-of select="axiom"/></td>
		</tr>
		<tr>
		<th align="left">Reference Cardinality</th>
		<td><xsl:value-of select="referenceCardinality"/></td>
		</tr>
		<tr>
		<th align="left">Number of Confirmations</th>
		<td><xsl:value-of select="numConfirmations"/></td>
		</tr>
		<tr>
		<th align="left">Number of Exceptions</th>
		<td><xsl:value-of select="numExceptions"/></td>
		</tr>
		<tr>
		<th align="left">Possibility</th>
		<td><xsl:value-of select="possibility"/></td>
		</tr>
		<tr>
		<th align="left">Necessity</th>
		<td><xsl:value-of select="necessity"/></td>
		</tr>
		<tr>
		<th align="left">Elapsed Time</th>
		<td><xsl:value-of select="elapsedTime"/></td>
		</tr>
		<xsl:variable name="numberOfConfirmations">
			<xsl:value-of select="count(confirmations)+1"/>
		</xsl:variable>
		<tr>
		<th align="left" rowspan="{$numberOfConfirmations}">Confirmations</th>
		</tr>
		<xsl:for-each select="confirmations">
		<tr>
		<td><xsl:value-of select="."/></td>
		</tr>
		</xsl:for-each>
		<xsl:variable name="numberOfExceptions">
			<xsl:value-of select="count(exceptions)+1"/>
		</xsl:variable>
		<tr>
		<th align="left" rowspan="{$numberOfExceptions}">Exceptions</th>
		</tr>
		<xsl:for-each select="exceptions">
		<tr>
		<td><xsl:value-of select="."/></td>
		</tr>
		</xsl:for-each>
		</xsl:for-each>
		</table>
		</body>
    </html>
  </xsl:template>

</xsl:stylesheet>
