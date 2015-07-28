package org.labrad
package tray

import com.google.inject.AbstractModule
import java.awt.{Desktop, Image, MenuItem, PopupMenu, SystemTray, Toolkit, TrayIcon}
import java.awt.event.{ActionEvent, ActionListener}
import java.net.URI
import javax.inject._
import org.labrad.util.Logging
import org.slf4j.{Logger, LoggerFactory}
import play.api.Application
import play.api.inject.ApplicationLifecycle
import scala.concurrent.Future


class TrayModule extends AbstractModule {
  def configure() = {
    if (SystemTray.isSupported) {
      bind(classOf[TrayController]).asEagerSingleton()
    }
  }
}

class TrayController @Inject() (app: Application, lifecycle: ApplicationLifecycle) extends Logging {

  // create popup menu
  private val browseItem = new MenuItem("Manage")
  browseItem.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent): Unit = {
      browse()
    }
  })

  private val exitItem = new MenuItem("Exit")
  exitItem.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent): Unit = {
      SystemTray.getSystemTray.remove(trayIcon)
      app.stop()
      sys.exit(0)
    }
  })

  private val popup = new PopupMenu
  popup.add(browseItem)
  popup.addSeparator()
  popup.add(exitItem)

  // create tray icon
  private val trayIcon = new TrayIcon(createImage("TrayIcon.png", "tray icon"))
  trayIcon.setPopupMenu(popup)
  trayIcon.setImageAutoSize(true)
  trayIcon.setToolTip("LabRAD")
  trayIcon.addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent): Unit = {
      browse()
    }
  })

  try {
    SystemTray.getSystemTray.add(trayIcon)
  } catch {
    case e: Throwable =>
      log.error("TrayIcon could not be added", e)
      app.stop()
      sys.exit(-1)
  }

  lifecycle.addStopHook { () =>
    SystemTray.getSystemTray.remove(trayIcon)
    Future.successful(())
  }


  private def browse(): Unit = {
    try {
      Desktop.getDesktop.browse(new URI("http://localhost:7667"))
    } catch {
      case e: Exception =>
        trayIcon.displayMessage("LabRAD", "Unable to launch web browser", TrayIcon.MessageType.ERROR)
        log.error("Unable to launch web browser", e)
    }
  }

  protected def createImage(path: String, description: String): Image = {
    val imageURL = getClass.getResource(path)
    if (imageURL == null) {
      log.error(s"Resource not found: $path")
      null
    } else {
      Toolkit.getDefaultToolkit.getImage(imageURL)
    }
  }
}
