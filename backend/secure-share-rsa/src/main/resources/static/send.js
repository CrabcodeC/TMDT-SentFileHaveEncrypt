document.getElementById("sendForm").addEventListener("submit", async function(e) {
  e.preventDefault();

  const fileInput = document.getElementById("file");
  const sender    = document.getElementById("sender").value;
  const recipient = document.getElementById("recipient").value;

  if (sender === recipient) { alert("Người gửi và người nhận không được trùng nhau!"); return; }
  if (!fileInput.files || !fileInput.files[0]) { alert("Vui lòng chọn file!"); return; }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);
  formData.append("recipient", recipient);
  formData.append("sender", sender);

  const status = document.getElementById("status");
  status.innerHTML = "⏳ Đang xử lý... (AES-256 + RSA-2048 Hybrid Encryption)";

  try {
    const response = await fetch("/api/send", { method: "POST", body: formData });
    const data = await response.json();

    if (!response.ok) throw new Error(data.error || "Lỗi server: " + response.status);

    // Lưu vào sessionStorage để trang verify có thể dùng
    sessionStorage.setItem("lastSend", JSON.stringify(data));

    status.innerHTML = `
      <div style="background:#052e16; border:2px solid #16a34a; padding:18px; border-radius:10px; margin-top:12px">
        <p style="color:#4ade80; font-size:16px; font-weight:bold; margin-bottom:12px">✅ Gửi file thành công!</p>
        <p>📤 Người gửi: <b>${sender}</b> &nbsp;→&nbsp; 📥 Người nhận: <b>${data.recipient}</b></p>
        <p>📄 File: <b>${data.fileName}</b> &nbsp;|&nbsp; 🔐 AES-256 + RSA-2048</p>

        <hr style="border-color:#1e3a2e; margin:14px 0"/>
        <p style="color:#94a3b8; font-size:13px; margin-bottom:6px">
          📋 <b>Copy 2 thông tin dưới đây</b> để dùng khi Xác Thực & Giải Mã:
        </p>

        <label style="color:#7dd3fc; font-size:12px; display:block; margin-top:10px">🔑 Encrypted Session Key:</label>
        <textarea id="copyKey" readonly style="width:100%;height:60px;background:#0f172a;color:#34d399;
          font-size:10px;padding:8px;border:1px solid #334155;border-radius:4px;font-family:monospace"
        >${data.encryptedSessionKey}</textarea>
        <button onclick="copyField('copyKey')" style="margin-top:4px;padding:4px 10px;font-size:12px">📋 Copy</button>

        <label style="color:#7dd3fc; font-size:12px; display:block; margin-top:12px">✍️ Signature (Chữ ký số):</label>
        <textarea id="copySig" readonly style="width:100%;height:60px;background:#0f172a;color:#fb923c;
          font-size:10px;padding:8px;border:1px solid #334155;border-radius:4px;font-family:monospace"
        >${data.signature}</textarea>
        <button onclick="copyField('copySig')" style="margin-top:4px;padding:4px 10px;font-size:12px">📋 Copy</button>

        <div style="margin-top:14px">
          <a href="/verify" style="background:#00ffd5;color:#0f172a;padding:10px 18px;
            border-radius:6px;text-decoration:none;font-weight:bold;">
            🔍 Đi đến trang Xác Thực & Giải Mã →
          </a>
        </div>
      </div>
    `;

  } catch(error) {
    status.innerHTML = `<p style="color:#f87171; padding:12px; background:#2d0a0a; border-radius:8px">❌ Lỗi: ${error.message}</p>`;
  }
});

function copyField(id) {
  const el = document.getElementById(id);
  navigator.clipboard.writeText(el.value).then(() => alert('✅ Đã copy!'));
}
