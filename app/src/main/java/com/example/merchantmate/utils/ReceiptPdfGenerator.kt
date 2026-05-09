package com.example.merchantmate.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.provider.MediaStore
import com.example.merchantmate.data.model.Transaction

object ReceiptPdfGenerator {

    fun generateReceiptPdf(
        context: Context,
        transaction: Transaction
    ): String {
        val pdfDocument = PdfDocument()

        val pageWidth = 595
        val pageHeight = 842

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Background
        canvas.drawColor(Color.WHITE)

        // Paints
        val headerPaint = Paint().apply {
            color = Color.rgb(24, 31, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val brandPaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val taglinePaint = Paint().apply {
            color = Color.rgb(220, 225, 230)
            textSize = 12f
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.rgb(35, 35, 35)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val headingPaint = Paint().apply {
            color = Color.rgb(35, 35, 35)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(70, 70, 70)
            textSize = 12f
            isAntiAlias = true
        }

        val smallPaint = Paint().apply {
            color = Color.rgb(120, 120, 120)
            textSize = 10f
            isAntiAlias = true
        }

        val whiteTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(225, 225, 225)
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val lightFillPaint = Paint().apply {
            color = Color.rgb(247, 249, 252)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(238, 242, 247)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val totalBoxPaint = Paint().apply {
            color = Color.rgb(24, 31, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val successPaint = Paint().apply {
            color = Color.rgb(39, 174, 96)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val marginLeft = 40f
        val marginRight = 555f

        // Header
        canvas.drawRoundRect(
            RectF(30f, 30f, 565f, 125f),
            18f,
            18f,
            headerPaint
        )

        canvas.drawText("MerchantMate", marginLeft, 70f, brandPaint)
        canvas.drawText("Smart Sales Companion for Micro-Merchants", marginLeft, 94f, taglinePaint)

        drawRightText(
            canvas = canvas,
            text = "RECEIPT",
            x = marginRight - 10f,
            y = 70f,
            paint = whiteTextPaint
        )

        drawRightText(
            canvas = canvas,
            text = transaction.receiptId,
            x = marginRight - 10f,
            y = 94f,
            paint = taglinePaint
        )

        var y = 160f

        // Receipt Summary Card
        canvas.drawRoundRect(
            RectF(40f, y, 555f, y + 95f),
            14f,
            14f,
            lightFillPaint
        )

        canvas.drawRoundRect(
            RectF(40f, y, 555f, y + 95f),
            14f,
            14f,
            linePaint
        )

        y += 30f
        canvas.drawText("Receipt Details", 60f, y, titlePaint)

        y += 28f
        drawLabelValue(canvas, "Receipt ID", transaction.receiptId, 60f, y, bodyPaint, headingPaint)
        drawLabelValue(canvas, "Payment", transaction.paymentMethod, 315f, y, bodyPaint, headingPaint)

        y += 26f
        drawLabelValue(canvas, "Status", transaction.status, 60f, y, bodyPaint, successPaint)
        drawLabelValue(canvas, "Items", transaction.items.size.toString(), 315f, y, bodyPaint, headingPaint)

        y += 55f

        // Items Title
        canvas.drawText("Purchased Items", 40f, y, titlePaint)
        y += 25f

        // Table Header
        val tableLeft = 40f
        val tableRight = 555f
        val tableTop = y
        val rowHeight = 34f

        canvas.drawRoundRect(
            RectF(tableLeft, tableTop, tableRight, tableTop + rowHeight),
            10f,
            10f,
            tableHeaderPaint
        )

        canvas.drawText("Item", 55f, y + 22f, headingPaint)
        canvas.drawText("Qty", 315f, y + 22f, headingPaint)
        canvas.drawText("Price", 370f, y + 22f, headingPaint)
        canvas.drawText("Total", 480f, y + 22f, headingPaint)

        y += rowHeight

        // Table Rows
        transaction.items.forEachIndexed { index, item ->
            val lineTotal = item.price * item.quantity

            if (index % 2 == 0) {
                val rowPaint = Paint().apply {
                    color = Color.rgb(252, 252, 252)
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }

                canvas.drawRect(
                    tableLeft,
                    y,
                    tableRight,
                    y + rowHeight,
                    rowPaint
                )
            }

            val itemName = shortenText(item.name, 30)

            canvas.drawText(itemName, 55f, y + 22f, bodyPaint)
            canvas.drawText(item.quantity.toString(), 318f, y + 22f, bodyPaint)
            canvas.drawText("£%.2f".format(item.price), 370f, y + 22f, bodyPaint)
            canvas.drawText("£%.2f".format(lineTotal), 480f, y + 22f, headingPaint)

            canvas.drawLine(tableLeft, y + rowHeight, tableRight, y + rowHeight, linePaint)

            y += rowHeight
        }

        y += 30f

        // Total Box
        canvas.drawRoundRect(
            RectF(315f, y, 555f, y + 65f),
            14f,
            14f,
            totalBoxPaint
        )

        val totalLabelPaint = Paint().apply {
            color = Color.rgb(220, 225, 230)
            textSize = 12f
            isAntiAlias = true
        }

        val totalAmountPaint = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Grand Total", 335f, y + 24f, totalLabelPaint)
        canvas.drawText("£%.2f".format(transaction.total), 335f, y + 52f, totalAmountPaint)

        y += 105f

        // Thank You Section
        val thankYouPaint = Paint().apply {
            color = Color.rgb(35, 35, 35)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Thank you for your purchase!", 40f, y, thankYouPaint)
        y += 24f
        canvas.drawText(
            "This receipt was generated by MerchantMate.",
            40f,
            y,
            bodyPaint
        )

        // Footer
        canvas.drawLine(40f, 785f, 555f, 785f, linePaint)

        drawCenteredText(
            canvas = canvas,
            text = "MerchantMate • Smart POS and Sales Insights",
            centerX = pageWidth / 2f,
            y = 812f,
            paint = smallPaint
        )

        pdfDocument.finishPage(page)

        val fileName = "${transaction.receiptId}.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS + "/MerchantMateReceipts"
            )
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to create PDF file in Downloads")

        resolver.openOutputStream(uri).use { outputStream ->
            if (outputStream == null) {
                throw Exception("Failed to open PDF output stream")
            }

            pdfDocument.writeTo(outputStream)
        }

        pdfDocument.close()

        return "Downloads/MerchantMateReceipts/$fileName"
    }

    private fun drawLabelValue(
        canvas: Canvas,
        label: String,
        value: String,
        x: Float,
        y: Float,
        labelPaint: Paint,
        valuePaint: Paint
    ) {
        canvas.drawText("$label:", x, y, labelPaint)
        canvas.drawText(value, x + 85f, y, valuePaint)
    }

    private fun drawRightText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text)
        canvas.drawText(text, x - textWidth, y, paint)
    }

    private fun drawCenteredText(
        canvas: Canvas,
        text: String,
        centerX: Float,
        y: Float,
        paint: Paint
    ) {
        val textWidth = paint.measureText(text)
        canvas.drawText(text, centerX - textWidth / 2f, y, paint)
    }

    private fun shortenText(text: String, maxLength: Int): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - 3) + "..."
        }
    }
}