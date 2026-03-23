import solwrLogo from '@/assets/solwr_logo.svg';
import html2canvas from 'html2canvas-pro';
import jsPdf from 'jspdf';
import type { Ref } from 'vue/dist/vue.js';


export async function exportToPdf(tag: string, releaseNoteRef: Ref<HTMLDivElement | undefined>) {
  if (!tag) return;

  try {
    const canvas = await html2canvas(releaseNoteRef.value as HTMLDivElement, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff',
      onclone: (clonedDoc) => {
        clonedDoc.querySelectorAll('[data-pdf-exclude]').forEach((el) => el.remove());
        clonedDoc.querySelectorAll('*').forEach((el) => {
          (el as HTMLElement).style.color = '#000000';
          (el as HTMLElement).style.backgroundColor = 'transparent';
        });
      }
    });

    const margin = 30;
    const pdf = new jsPdf('p', 'mm', 'a4');

    const fullPageWidth = pdf.internal.pageSize.getWidth();
    const fullPageHeight = pdf.internal.pageSize.getHeight();
    const pageWidth = fullPageWidth - margin * 2;
    const pageHeight = fullPageHeight - margin * 2;

    // How many canvas pixels correspond to one page of content
    const scaleFactor = canvas.width / pageWidth;
    const sliceHeight = pageHeight * scaleFactor;

    let yOffset = 0;
    let page = 0;

    while (yOffset < canvas.height) {
      if (page > 0) pdf.addPage();

      const currentSliceHeight = Math.min(sliceHeight, canvas.height - yOffset);

      // Create a canvas slice for this page
      const pageCanvas = document.createElement('canvas');
      pageCanvas.width = canvas.width;
      pageCanvas.height = currentSliceHeight;
      pageCanvas.getContext('2d')!.drawImage(
        canvas,
        0, yOffset, canvas.width, currentSliceHeight,
        0, 0, canvas.width, currentSliceHeight,
      );

      const pageImgData = pageCanvas.toDataURL('image/png');
      const sliceHeightMm = currentSliceHeight / scaleFactor;
      pdf.addImage(pageImgData, 'PNG', margin, margin, pageWidth, sliceHeightMm);

      yOffset += sliceHeight;
      page++;
    }

    // Convert SVG to PNG data URL with black color and preserved aspect ratio
    const logoPng = await new Promise<string>((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const c = document.createElement('canvas');
        c.width = img.naturalWidth;
        c.height = img.naturalHeight;
        const ctx = c.getContext('2d')!;
        ctx.drawImage(img, 0, 0);
        // Recolor to black
        ctx.globalCompositeOperation = 'source-in';
        ctx.fillStyle = '#000000';
        ctx.fillRect(0, 0, c.width, c.height);
        resolve(c.toDataURL('image/png'));
      };
      img.onerror = reject;
      img.src = solwrLogo;
    });

    // Add logo to top-right of every page (preserved 200:30 aspect ratio)
    const logoWidth = 40;
    const logoHeight = logoWidth * (30 / 200); // = 6mm
    const logoX = pdf.internal.pageSize.getWidth() - margin / 2 - logoWidth;
    const logoY = margin / 2;
    const pageCount = pdf.getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      pdf.setPage(i);
      pdf.addImage(logoPng, 'PNG', logoX, logoY, logoWidth, logoHeight);
    }

    pdf.save(`${tag}.pdf`);
  } catch (error) {
    console.error('Error exporting to PDF:', error);
  }
}