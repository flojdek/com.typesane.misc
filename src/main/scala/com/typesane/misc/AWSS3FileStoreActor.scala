package com.typesane.misc

import akka.actor.Actor
import akka.actor.Props
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.nio.PngWriter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream

object ImageStoreActor {
  case class StoreImage(imgFile: File, fitWidth: Int, fitHeight: Int)
  case class ImageURL(url: String)
}

object AWSS3ImageStoreActor {
  def props(AWSS3BucketName: String,
            AWSAccessKeyID: String,
            AWSSecretAccessKey: String): Props = Props(new AWSS3ImageStoreActor(AWSS3BucketName,
                                                                                AWSAccessKeyID,
                                                                                AWSSecretAccessKey))
}

class AWSS3ImageStoreActor(AWSS3BucketName: String,
                           AWSAccessKeyID: String,
                           AWSSecretAccessKey: String) extends Actor {
  import ImageStoreActor._

  val AWSCredentials = new BasicAWSCredentials(AWSAccessKeyID, AWSSecretAccessKey)
  val AWSS3Client = new AmazonS3Client(AWSCredentials)

  def receive = {
    case StoreImage(imgFile: File, fitWidth: Int, fitHeight: Int) => {
      // Scale.
      val scaledPNG: ByteArrayInputStream = Image.fromStream(new FileInputStream(imgFile))
                                                 .fit(fitWidth, fitHeight, java.awt.Color.BLACK)
                                                 .stream(PngWriter.NoCompression)
      // Total size here, as 'ByteArrayInputStream' knows the size.
      val contentLength = scaledPNG.available()

      // Generate unique file name.
      val nowDateTime = java.time.LocalDateTime.now
      val uniqueFileName = HashUtil.sha1(nowDateTime.toString + imgFile.getName())

      // Set content lenght on metadata.
      val AWSS3ObjectMetadata = new ObjectMetadata()
      AWSS3ObjectMetadata.setContentLength(contentLength)

      // Store. The 'putObject' call is synchronous!
      val AWSS3ScaledFilePath = nowDateTime.toLocalDate.toString + "/" + uniqueFileName + ".png"
      val scaledResult = AWSS3Client.putObject(new PutObjectRequest(AWSS3BucketName,
                                                                    AWSS3ScaledFilePath,
                                                                    scaledPNG,
                                                                    AWSS3ObjectMetadata))

      // Send image URL.
      sender() ! ImageURL(AWSS3Client.getResourceUrl(AWSS3BucketName, AWSS3ScaledFilePath))
    }
  }
}
