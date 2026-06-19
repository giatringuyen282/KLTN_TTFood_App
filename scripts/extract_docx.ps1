$word = New-Object -ComObject Word.Application
$word.Visible = $false
$doc = $word.Documents.Open("D:\43_KLTN_TTFood\UI Grabfood.docx")
$text = $doc.Content.Text
$text | Out-File -FilePath "D:\43_KLTN_TTFood\ui_grabfood_text.txt" -Encoding UTF8
$doc.Close([ref]$false)
$word.Quit()
