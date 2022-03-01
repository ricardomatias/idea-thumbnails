package com.hamoid.openrndrThumbnails

import java.awt.Image
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException

class TransferableImage(private val image: Image) : Transferable {
    override fun getTransferDataFlavors() =
        arrayOf(DataFlavor.imageFlavor)

    override fun isDataFlavorSupported(flavor: DataFlavor?) =
        DataFlavor.imageFlavor.equals(flavor)


    override fun getTransferData(flavor: DataFlavor?): Any {
        if (!DataFlavor.imageFlavor.equals(flavor)) {
            throw UnsupportedFlavorException(flavor)
        }
        return image
    }
}
