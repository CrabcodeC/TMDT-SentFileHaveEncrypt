document
  .getElementById("sendForm")
  .addEventListener("submit", async function (e) {
    e.preventDefault();

    const fileInput = document.getElementById("file");
    const sender = document.getElementById("sender").value;
    const recipient = document.getElementById("recipient").value;

    if (sender === recipient) {
      alert("Người gửi và người nhận không được trùng nhau!");
      return;
    }

    if (!fileInput.files || !fileInput.files[0]) {
      alert("Vui lòng chọn file!");
      return;
    }

    const formData = new FormData();
    formData.append("file", fileInput.files[0]);
    formData.append("recipient", recipient);
    formData.append("sender", sender);

    const status = document.getElementById("status");
    status.innerHTML = "⏳ Đang xử lý... (AES + RSA Hybrid Encryption)";

    try {
      const response = await fetch("/api/send", {
        method: "POST",
        body: formData,
      });

      const data = await response.json();

      if (response.ok) {
        status.innerHTML = `
          <div style="color:#00cc44; background:#001a0d; padding:16px; border-radius:8px; border:1px solid #00cc44">
            <p>✅ <b>Gửi file thành công!</b></p>
            <p>📤 Người gửi: <b>${sender}</b></p>
            <p>📥 Người nhận: <b>${data.recipient}</b></p>
            <p>📄 File: <b>${data.fileName}</b></p>
            <p>🔐 Đã mã hóa bằng AES-256 + RSA-2048</p>
            <p style="font-size:12px; color:#aaa">Encrypted Session Key (đầu): ${data.encryptedSessionKey ? data.encryptedSessionKey.substring(0, 40) + "..." : "N/A"}</p>
          </div>
        `;
      } else {
        throw new Error(data.error || "Lỗi server: " + response.status);
      }
    } catch (error) {
      status.innerHTML = `<p style="color:red">❌ Lỗi: ${error.message}</p>`;
    }
  });
