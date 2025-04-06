import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import LabelEncoder

# 📌 Charger les données JSON
data = pd.read_json("user_agent_info.json")

# 🚀 1️⃣ TRAITEMENT DES VALEURS MANQUANTES
data.fillna("Unknown", inplace=True)


# 🚀 3️⃣ ENCODAGE DES VARIABLES CATÉGORIQUES
label_encoders = {}
categorical_columns = ["browserName", "deviceName", "clientIp"]

for col in categorical_columns:
	label_encoders[col] = LabelEncoder()
	data[col] = label_encoders[col].fit_transform(data[col])

# 🚀 4️⃣ SÉLECTION DES FEATURES
X = data[["browserName", "deviceName", "clientIp"]]

# 🚀 5️⃣ DÉTECTION D'ANOMALIES AVEC ISOLATION FOREST
model = IsolationForest(contamination=0.2, random_state=42)  # Set contamination to 20%
data["Anomaly"] = model.fit_predict(X)

# Conversion : -1 (anomalie) → 1, et 1 (normal) → 0
data["Anomaly"] = data["Anomaly"].apply(lambda x: 1 if x == -1 else 0)

# 📌 Convertir les résultats en JSON
result_json = data[["browserName", "deviceName", "clientIp", "Anomaly"]].to_dict(orient="records")

# ✅ Affichage des anomalies détectées
print(result_json)
