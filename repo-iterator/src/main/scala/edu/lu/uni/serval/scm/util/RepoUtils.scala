package edu.lu.uni.serval.scm.util


import org.json4s._
import org.json4s.native.JsonMethods._

import java.io.File
import java.util.ArrayList
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import java.io.StringWriter
import org.apache.commons.io.IOUtils
import org.apache.http.entity.ContentType


/**
 * @author darkrsw
 */
object RepoUtils
{
	// this is only for instant check
	def main(args: Array[String]): Unit =
	{
		val util = new RepoUtils
		val repoOwner = args(0)
		val repoName = args(1)
		val ck = util checkPomExists(repoOwner, repoName)
		
		Console.println("%s/%s has pom.xml: ".format(repoOwner, repoName) + ck)
	}
	
	@deprecated
	def checkPomExists(repoPath: String): Boolean =
	{
		val pomFile = new File(repoPath + "/pom.xml")
		
		return pomFile.exists()
	}
}

// need to transform this into scala style singleton.
class RepoUtils 
{
	val httpClient = HttpClients.createDefault();	
	val socketTimeout = 10*1000;
	val rootTreeRequestUrlTemplate = "https://api.github.com/repos/%s/%s/git/trees/HEAD"
	val token = """0d869e8bf4e3874c51f10541cb5db4587291b571"""
		
	def checkPomExists(repoOwner: String, repoName: String): Boolean =
	{
		val requestURL = rootTreeRequestUrlTemplate.format(repoOwner, repoName)
		
		val requestConfig = RequestConfig.custom()
			        .setSocketTimeout(socketTimeout)
			        .setConnectTimeout(socketTimeout)
			        .build();
			
		val httpGet = new HttpGet(requestURL);
		
		httpGet.addHeader( "Authorization", "token "+ token );
		httpGet.addHeader("User-Agent", "false-alarm-study");
		httpGet.setConfig(requestConfig);
		
		val response = httpClient.execute( httpGet );
		
		val status = response.getStatusLine.getStatusCode
		
		val entity = response.getEntity();

		val contentType = ContentType.getOrDefault(entity);
		val charset = contentType.getCharset();
		val writer = new StringWriter();
		IOUtils.copy(entity.getContent(), writer, charset);
		val theString = writer.toString();

		val ret = if(status == 200)
		{
			val jsonRoot = parse(theString)
			
			val paths = for {
				JObject(tree) <- jsonRoot
				JField("path", JString(path)) <- tree
			} yield path
			
			if(paths.contains("pom.xml"))
				true
			else
				false
		}
		else
		{
			false
		}
		
		response.close
		return ret;
	}
}