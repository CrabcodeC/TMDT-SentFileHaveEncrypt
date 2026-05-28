const form = document.getElementById("sendForm");

form.addEventListener("submit", async function (e) {
  e.preventDefault();

  const fileInput = document.getElementById("file");
  const sender = document.getElementById("sender").value;
  const recipient = document.getElementById("recipient").value;

  if (!fileInput.files[0]) {
    alert("Vui lòng chọn file!");
    return;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);
  formData.append("recipient", recipient);
  formData.append("sender", sender);

  const status = document.getElementById("status");
  status.innerHTML = "Đang xử lý bảo mật... (AES + RSA)";

  try {
    const response = await fetch("/send", {
      method: "POST",
      body: formData,
    });

    const data = await response.json();

    status.innerHTML = `
            <p style="color: #00ffd5;">✅ Gửi file thành công!</p>
            <p>Người nhận: <b>${data.recipient}</b></p>
            <p>File: <b>${data.fileName}</b></p>
            <small>Đã mã hóa AES + Phong bì RSA + Chữ ký số</small>
        `;

    console.log("Response:", data);
  } catch (error) {
    status.innerHTML = `<p style="color: red;">❌ Lỗi: ${error.message}</p>`;
  }
});
