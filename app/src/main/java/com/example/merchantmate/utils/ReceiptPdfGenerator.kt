package com.example.merchantmate.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.merchantmate.data.model.Transaction
import java.io.File
import java.io.FileOutputStream

object ReceiptPdfGenerator {

    fun generateReceiptPdf(
        context: Context,
        transaction: Transaction
    ): File {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
        }

        val headingPaint = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            textSize = 13f
        }

        var y = 60f

        canvas.drawText("MerchantMate Receipt", 40f, y, titlePaint)
        y += 40f

        canvas.drawText("Receipt ID: ${transaction.receiptId}", 40f, y, bodyPaint)
        y += 24f

        canvas.drawText("Payment: ${transaction.paymentMethod}", 40f, y, bodyPaint)
        y += 24f

        canvas.drawText("Status: ${transaction.status}", 40f, y, bodyPaint)
        y += 36f

        canvas.drawText("Items", 40f, y, headingPaint)
        y += 28f

        transaction.items.forEach { item ->
            val lineTotal = item.price * item.quantity

            canvas.drawText(item.name, 40f, y, bodyPaint)
            y += 22f

            canvas.drawText(
                "Qty: ${item.quantity} x £%.2f = £%.2f".format(item.price, lineTotal),
                60f,
                y,
                bodyPaint
            )
            y += 28f
        }

        y += 20f
        canvas.drawText("Grand Total: £%.2f".format(transaction.total), 40f, y, titlePaint)

        pdfDocument.finishPage(page)

        val folder = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "MerchantMateReceipts"
        )

        if (!folder.exists()) {
            folder.mkdirs()
        }

        val file = File(folder, "${transaction.receiptId}.pdf")

        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        return file
    }
}